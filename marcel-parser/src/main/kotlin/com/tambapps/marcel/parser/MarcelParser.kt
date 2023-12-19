package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ParserUtils.UNARY_PRIORITY
import com.tambapps.marcel.parser.cst.AbstractMethodNode
import com.tambapps.marcel.parser.cst.AnnotationNode
import com.tambapps.marcel.parser.cst.ClassNode
import com.tambapps.marcel.parser.cst.ConstructorNode
import com.tambapps.marcel.parser.cst.AccessNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldNode
import com.tambapps.marcel.parser.cst.MethodNode
import com.tambapps.marcel.parser.cst.MethodParameterNode
import com.tambapps.marcel.parser.cst.ScriptNode
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatNode
import com.tambapps.marcel.parser.cst.expression.literal.IntNode
import com.tambapps.marcel.parser.cst.expression.literal.LongNode
import com.tambapps.marcel.parser.cst.expression.literal.MapNode
import com.tambapps.marcel.parser.cst.expression.literal.NullNode
import com.tambapps.marcel.parser.cst.expression.literal.StringNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorNode
import com.tambapps.marcel.parser.cst.expression.LambdaNode
import com.tambapps.marcel.parser.cst.expression.NotNode
import com.tambapps.marcel.parser.cst.expression.SwitchNode
import com.tambapps.marcel.parser.cst.expression.TernaryNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusNode
import com.tambapps.marcel.parser.cst.expression.WhenNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolNode
import com.tambapps.marcel.parser.cst.expression.literal.CharNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexNode
import com.tambapps.marcel.parser.cst.expression.reference.*
import com.tambapps.marcel.parser.cst.imprt.ImportNode
import com.tambapps.marcel.parser.cst.imprt.SimpleImportNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportNode
import com.tambapps.marcel.parser.cst.statement.BlockNode
import com.tambapps.marcel.parser.cst.statement.BreakNode
import com.tambapps.marcel.parser.cst.statement.ContinueNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.cst.statement.ForInNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarNode
import com.tambapps.marcel.parser.cst.statement.ForVarNode
import com.tambapps.marcel.parser.cst.statement.IfStatementNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationNode
import com.tambapps.marcel.parser.cst.statement.ReturnNode
import com.tambapps.marcel.parser.cst.statement.StatementNode
import com.tambapps.marcel.parser.cst.statement.ThrowNode
import com.tambapps.marcel.parser.cst.statement.TryCatchNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.cst.statement.WhileNode
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class MarcelParser constructor(private val classSimpleName: String, tokens: List<LexToken>) {

  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens)

  private val tokens = tokens.filter { it.type != TokenType.WHITE_SPACE }

  private var currentIndex = 0

  private val current: LexToken
    get() {
      checkEof()
      return tokens[currentIndex]
    }
  private val previous get() = tokens[currentIndex - 1]

  private val eof: Boolean
    get() = currentIndex >= tokens.size
  private val currentSafe: LexToken?
    get() = if (eof) null else tokens[currentIndex]

  private val errors = mutableListOf<MarcelParserException.Error>()

  fun parse(): SourceFileNode {
    if (tokens.isEmpty()) {
      throw MarcelParserException(
        LexToken(
          0,
          0,
          0,
          0,
          TokenType.END_OF_FILE,
          null
        ), "Unexpected end of file"
      )
    }
    val packageName = parseOptPackage()

    val dumbbells = mutableListOf<String>()
    while (current.type == TokenType.DUMBBELL) {
      dumbbells.add(dumbbell())
    }

    val sourceFile = SourceFileNode(packageName = packageName, tokenStart = tokens.first(),
      dumbbells = dumbbells,
      tokenEnd = tokens.last())
    val scriptNode = ScriptNode(tokens.first(), tokens.last(),
      if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName)

    val (imports, extensionTypes) = parseImports(sourceFile)
    sourceFile.imports.addAll(imports)
    sourceFile.extensionImports.addAll(extensionTypes)

    while (current.type != TokenType.END_OF_FILE) {
      parseMember(packageName, sourceFile, scriptNode, null)
    }

    // class defined are not script class inner classes
    sourceFile.classes.addAll(scriptNode.innerClasses)
    scriptNode.innerClasses.clear()

    if (scriptNode.isNotEmpty) sourceFile.script = scriptNode
    if (errors.isNotEmpty()) {
      throw MarcelParserException(errors)
    }
    return sourceFile
  }
  private fun parseMember(packageName: String?, parentNode: CstNode?, classNode: ClassNode, outerClassNode: ClassNode?) {
    val annotations = parseAnnotations(parentNode)
    val access = parseAccess(parentNode)
    if (current.type == TokenType.CLASS || current.type == TokenType.EXTENSION) {
      classNode.innerClasses.add(parseClass(packageName, parentNode, annotations, access, outerClassNode))
    } else if (current.type == TokenType.FUN || current.type == TokenType.CONSTRUCTOR) {
      when (val method = method(classNode, annotations, access)) {
        is MethodNode -> classNode.methods.add(method)
        is ConstructorNode -> classNode.constructors.add(method)
      }
    } else if (classNode is ScriptNode) {
      if (annotations.isNotEmpty() || access.isExplicit) {
        // class fields in script always have access specified, or annotations
        classNode.fields.add(field(classNode, annotations, access))
      } else {
        classNode.runMethodStatements.add(statement(parentNode))
      }
    } else {
      // it must be a field
      classNode.fields.add(field(classNode, annotations, access))
    }
  }

  private fun parseImports(parentNode: CstNode?): Pair<List<ImportNode>, List<TypeNode>>  {
    val imports = mutableListOf<ImportNode>()
    val extensionTypes = mutableListOf<TypeNode>()
    while (current.type == TokenType.IMPORT) {
      if (lookup(1)?.type == TokenType.EXTENSION) {
        skip(2)
        extensionTypes.add(parseType(parentNode))
      } else {
        imports.add(import(parentNode))
      }
    }
    return Pair(imports, extensionTypes)
  }

  fun import(parentNode: CstNode? = null): ImportNode {
    val importToken = accept(TokenType.IMPORT)
    val staticImport = acceptOptional(TokenType.STATIC) != null
    val classParts = mutableListOf(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      if (current.type == TokenType.MUL) {
        if (staticImport) {
          throw MarcelParserException(
            current,
            "Invalid static import"
          )
        }
        skip()
        acceptOptional(TokenType.SEMI_COLON)
        return WildcardImportNode(parentNode, importToken, previous, classParts.joinToString(separator = "."))
      }
      classParts.add(accept(TokenType.IDENTIFIER).value)
    }
    if (classParts.size <= 1) {
      throw MarcelParserException(
        previous,
        "Invalid class full name" + classParts.joinToString(
          separator = "."
        )
      )
    }
    val node = if (staticImport) {
      val className = classParts.subList(0, classParts.size - 1).joinToString(separator = ".")
      val method = classParts.last()
      StaticImportNode(parentNode, importToken, previous, className, method)
    } else {
      val asName = if (acceptOptional(TokenType.AS) != null) accept(TokenType.IDENTIFIER).value else null
      SimpleImportNode(parentNode, importToken, previous, classParts.joinToString(separator = "."), asName)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return node
  }

  private fun parseOptPackage(): String? {
    if (acceptOptional(TokenType.PACKAGE) == null) return null
    val parts = mutableListOf<String>(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      parts.add(accept(TokenType.IDENTIFIER).value)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return parts.joinToString(separator = ".")
  }

  private fun dumbbell(): String {
    accept(TokenType.DUMBBELL)
    accept(TokenType.OPEN_SIMPLE_QUOTE)
    val d = simpleStringPart()
    accept(TokenType.CLOSING_SIMPLE_QUOTE)
    return d
  }

  fun field(parentNode: ClassNode, annotations: List<AnnotationNode>, access: AccessNode): FieldNode {
    val tokenStart = current
    val type = parseType(parentNode)
    val name = accept(TokenType.IDENTIFIER).value
    val initialValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
    acceptOptional(TokenType.SEMI_COLON)
    return FieldNode(parentNode, tokenStart, previous, access, annotations, type, name, initialValue)
  }

  private fun parseClass(packageName: String?, parentNode: CstNode?, annotations: List<AnnotationNode>, access: AccessNode,
                         outerClassNode: ClassNode? = null): ClassNode {
    val isExtensionClass = acceptOptional(TokenType.EXTENSION) != null
    val classToken = accept(TokenType.CLASS)
    val classSimpleName = accept(TokenType.IDENTIFIER).value
    val className =
      if (outerClassNode != null) "${outerClassNode.className}\$$classSimpleName"
      else if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName

    val forExtensionClassType = if (isExtensionClass) {
      accept(TokenType.FOR)
      parseType(parentNode)
    } else null

    val superType =
      if (acceptOptional(TokenType.EXTENDS) != null) parseType(parentNode)
      else null
    val interfaces = mutableListOf<TypeNode>()
    if (acceptOptional(TokenType.IMPLEMENTS) != null) {
      while (current.type == TokenType.IDENTIFIER) {
        interfaces.add(parseType(parentNode))
        acceptOptional(TokenType.COMMA)
      }
    }
    val classNode = ClassNode(classToken, classToken, access, className, superType, interfaces, forExtensionClassType)
    classNode.annotations.addAll(annotations)

    accept(TokenType.BRACKETS_OPEN)
    while (current.type != TokenType.BRACKETS_CLOSE) {
      parseMember(packageName, classNode, classNode, classNode)
    }
    accept(TokenType.BRACKETS_CLOSE)
    return classNode
  }

  fun method(parentNode: ClassNode, annotations: List<AnnotationNode>, access: AccessNode): AbstractMethodNode {
    val token = current
    val isConstructor = acceptOneOf(TokenType.FUN, TokenType.CONSTRUCTOR).type == TokenType.CONSTRUCTOR
    val node = if (!isConstructor) {
      val returnType = parseType(parentNode)
      val methodName = accept(TokenType.IDENTIFIER).value
      MethodNode(parentNode, token, token, access, methodName, returnType)
    } else ConstructorNode(parentNode, token, token, access)
    node.annotations.addAll(annotations)

    // parameters
    accept(TokenType.LPAR)
    val parameters = node.parameters
    while (current.type != TokenType.RPAR) {
      val parameterTokenStart = current
      val parameterAnnotations = parseAnnotations(parentNode)
      val isThisParameter = acceptOptional(TokenType.THIS) != null && acceptOptional(TokenType.DOT) != null
      val type =
        if (!isThisParameter) parseType(parentNode)
        else TypeNode(parentNode, "", emptyList(), 0, previous, previous)
      val parameterName = accept(TokenType.IDENTIFIER).value
      val defaultValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
      parameters.add(MethodParameterNode(parentNode, parameterTokenStart, previous, parameterName, type, defaultValue, parameterAnnotations, isThisParameter))
      acceptOptional(TokenType.COMMA)
    }
    skip() // skip RPAR

    val statements = mutableListOf<StatementNode>()
    // super/this method call if any
    if (isConstructor && current.type == TokenType.COLON) {
      skip()
      when (val atom = atom(parentNode)) {
        is SuperConstructorCallNode, is ThisConstructorCallNode  -> statements.add(ExpressionStatementNode(atom))
        else -> throw MarcelParserException(
          atom.token,
          "Expected this or super constructor call"
        )
      }
    }

    if (isConstructor && current.type != TokenType.BRACKETS_OPEN) {
      // constructor may not have a body
      node.statements.addAll(statements)
      return node
    }

    if (current.type == TokenType.ARROW) {
      (node as? MethodNode)?.isSingleStatementFunction = true
      // fun foo() -> expression()
      skip()
      statements.add(statement(parentNode))
    } else {
      accept(TokenType.BRACKETS_OPEN)
      while (current.type != TokenType.BRACKETS_CLOSE) {
        statements.add(statement(node))
      }
      skip()
    }
    node.statements.addAll(statements)
    return node
  }

  private fun parseAccess(parentNode: CstNode?): AccessNode {
    val startIndex = currentIndex
    val tokenStart = current
    val visibilityToken = acceptOptionalOneOf(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
      TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC
    val isStatic = acceptOptional(TokenType.STATIC) != null
    val isFinal = acceptOptional(TokenType.FINAL) != null
    val isInline = acceptOptional(TokenType.INLINE) != null
    val hasExplicitAccess = currentIndex > startIndex
    return AccessNode(parentNode, tokenStart, if (hasExplicitAccess) previous else current, isStatic, isInline, isFinal, visibilityToken, hasExplicitAccess)
  }

  internal fun parseType(parentNode: CstNode? = null): TypeNode {
    val tokenStart = next()
    // parts are inner class levels. in Marcel you can't specify classes full name, like marcel.lang.IntRange.
    // you have to import the class and then use its simple name. In case of conflict there is the import as
    val typeFragments = mutableListOf<String>(
      parseTypeFragment(tokenStart)
    )
    while (current.type == TokenType.DOT) {
      skip()
      typeFragments.add(parseTypeFragment(next()))
    }

    // generic types
    val genericTypes = mutableListOf<TypeNode>()
    if (current.type == TokenType.LT) {
      skip()
      genericTypes.add(parseType(parentNode))
      while (current.type == TokenType.COMMA) {
        skip()
        genericTypes.add(parseType(parentNode))
      }
      accept(TokenType.GT)
    }

    // array dimensions
    val arrayDimension = parseArrayDimensions()
    return TypeNode(parentNode, typeFragments.joinToString(separator = "$"), genericTypes, arrayDimension, tokenStart, previous)
  }

  private fun parseArrayDimensions(): Int {
    var arrayDimension = 0
    while (current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
      skip(2)
      arrayDimension++
    }
    return arrayDimension
  }

  private fun parseTypeFragment(token: LexToken) = when (token.type) {
    TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_VOID, TokenType.TYPE_CHAR,
    TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BYTE, TokenType.TYPE_SHORT, TokenType.IDENTIFIER -> token.value
    TokenType.TYPE_BOOL -> "boolean"
    TokenType.DYNOBJ -> "DynamicObject"
    TokenType.END_OF_FILE -> throw eofException(token)
    else -> throw MarcelParserException(
      token,
      "Doesn't handle type ${token.type}"
    )
  }

  fun statement(parentNode: CstNode? = null): StatementNode {
    val token = next()
    try {
      return when (token.type) {
        TokenType.RETURN -> {
          val expr =
            if (current.type != TokenType.SEMI_COLON && current.type != TokenType.BRACKETS_CLOSE) expression(parentNode)
          else null
          ReturnNode(parentNode, expr, expr?.tokenStart ?: token, expr?.tokenEnd ?: token)
        }
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT,
        TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID,
        TokenType.TYPE_CHAR, TokenType.DYNOBJ -> {
          rollback()
          varDecl(parentNode)
        }
        TokenType.IDENTIFIER -> {
          if ((current.type == TokenType.IDENTIFIER && lookup(1)?.type == TokenType.ASSIGNMENT
            || current.type == TokenType.LT)
            // for array var decl
            || (current.type == TokenType.DOT && lookup(1)?.type == TokenType.CLASS
                || current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE
                && lookup(2)?.type == TokenType.IDENTIFIER))  {
            rollback()
            varDecl(parentNode)
          } else {
            rollback()
            val expr = expression(parentNode)
            ExpressionStatementNode(parentNode, expr, expr.tokenStart, expr.tokenEnd)
          }
        }
        TokenType.BRACKETS_OPEN -> block(parentNode, acceptBracketOpen = false)
        TokenType.IF -> {
          accept(TokenType.LPAR)
          val condition = ifConditionExpression(parentNode)
          accept(TokenType.RPAR)
          val rootIf = IfStatementNode(condition, statement(parentNode), null, parentNode, token, previous)
          var currentIf = rootIf
          while (current.type == TokenType.ELSE) {
            skip()
            if (acceptOptional(TokenType.IF) != null) {
              accept(TokenType.LPAR)
              val elseIfCondition = ifConditionExpression(parentNode)
              accept(TokenType.RPAR)
              val newIf = IfStatementNode(elseIfCondition, statement(parentNode), null, parentNode, token, previous)
              currentIf.falseStatementNode = newIf
              currentIf = newIf
            } else {
              currentIf.falseStatementNode = statement(parentNode)
            }
          }
          rootIf
        }
        TokenType.WHILE -> {
          accept(TokenType.LPAR)
          val condition = ifConditionExpression(parentNode)
          accept(TokenType.RPAR)
          val statement = statement(parentNode)
          WhileNode(parentNode, token, statement.tokenEnd, condition, statement)
        }
        TokenType.THROW -> {
          val expression = expression(parentNode)
          ThrowNode(parentNode, token, previous, expression)
        }
        TokenType.DEF -> {
          accept(TokenType.LPAR)
          val declarations = mutableListOf<Pair<TypeNode, String>?>()
          while (current.type != TokenType.RPAR) {
            if (current.type == TokenType.IDENTIFIER && current.value.all { it == '_' }) {
              declarations.add(null)
              skip()
            } else {
              val varType = parseType(parentNode)
              val varName = accept(TokenType.IDENTIFIER).value
              declarations.add(Pair(varType, varName))
            }
            if (current.type == TokenType.COMMA) skip()
          }
          skip() // skip RPAR
          accept(TokenType.ASSIGNMENT)
          val expression = expression(parentNode)
          MultiVarDeclarationNode(parentNode, token, previous, declarations, expression)
        }
        TokenType.FOR -> {
          accept(TokenType.LPAR)
          if (current.type == TokenType.LPAR) {
            // multi var declaration
            accept(TokenType.LPAR)
            val declarations = mutableListOf<Pair<TypeNode, String>>()
            while (current.type != TokenType.RPAR) {
              val varType = parseType(parentNode)
              val varName = accept(TokenType.IDENTIFIER).value
              declarations.add(Pair(varType, varName))
              if (current.type == TokenType.COMMA) skip()
            }
            accept(TokenType.RPAR)
            if (declarations.isEmpty()) {
              throw MarcelParserException(token, "Cannot have for loop with no variables")
            }
            accept(TokenType.IN)
            val expression = expression(parentNode)
            accept(TokenType.RPAR)
            val forBlock = statement(parentNode)

            ForInMultiVarNode(declarations, expression, forBlock, parentNode, token, forBlock.tokenEnd)
          } else {
            val type = parseType(parentNode)
            if (lookup(1)?.type == TokenType.IN) {
              // for in statement
              val identifier = accept(TokenType.IDENTIFIER).value
              accept(TokenType.IN)
              val expression = expression(parentNode)
              accept(TokenType.RPAR)
              val forBlock = statement(parentNode)
              ForInNode(type, identifier, expression, forBlock, parentNode, token, previous)
            } else {
              // for (;;)
              // needed especially if initStatement is var declaration
              val initStatement = varDecl(parentNode, type)
              accept(TokenType.SEMI_COLON)
              val condition = expression(parentNode)
              accept(TokenType.SEMI_COLON)
              val iteratorStatement = statement(parentNode)
              accept(TokenType.RPAR)
              val forBlock = statement(parentNode)
              ForVarNode(initStatement, condition, iteratorStatement, forBlock, parentNode, token, forBlock.tokenEnd)
            }
          }
        }
        TokenType.BREAK -> BreakNode(parentNode, token)
        TokenType.CONTINUE -> ContinueNode(parentNode, token)
        TokenType.TRY -> {
          val resources = mutableListOf<VariableDeclarationNode>()
          if (current.type == TokenType.LPAR) {
            skip()
            while (current.type != TokenType.RPAR) {
              val e = statement(parentNode)
              if (e !is VariableDeclarationNode) throw MarcelParserException(
                e.token,
                "Can only declare variables in try with resources"
              )
              resources.add(e)
              if (current.type == TokenType.COMMA) skip()
            }
            skip() // skip RPAR
          }
          val tryNode = statement(parentNode)
          val catchNodes = mutableListOf<Triple<List<TypeNode>, String, StatementNode>>()
          while (current.type == TokenType.CATCH) {
            skip()
            accept(TokenType.LPAR)
            val exceptions = mutableListOf(
              parseType(parentNode)
            )
            while (current.type == TokenType.PIPE) {
              skip()
              exceptions.add(parseType(parentNode))
            }
            val exceptionVarName = accept(TokenType.IDENTIFIER).value
            accept(TokenType.RPAR)
            val statement = statement(parentNode)
            catchNodes.add(
              Triple(
                exceptions, exceptionVarName, statement
              ))
          }
          val finallyBlock = if (acceptOptional(TokenType.FINALLY) != null) statement(parentNode) else null
          TryCatchNode(parentNode, token, previous, tryNode, resources, catchNodes, finallyBlock)
        }
        else -> {
          rollback()
          val expr = expression(parentNode)
          ExpressionStatementNode(parentNode, expr, expr.tokenStart, expr.tokenEnd)
        }
      }
    } finally {
      acceptOptional(TokenType.SEMI_COLON)
    }
  }

  private fun ifConditionExpression(parentNode: CstNode?): ExpressionNode {
    return if (ParserUtils.isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER) {
      val type = parseType(parentNode)
      val variableName = accept(TokenType.IDENTIFIER).value
      accept(TokenType.ASSIGNMENT)
      val expression = expression(parentNode)
      TruthyVariableDeclarationNode(parentNode, type.tokenStart, expression.tokenEnd, type, variableName, expression)
    } else expression(parentNode)
  }

  private fun varDecl(parentNode: CstNode?) = varDecl(parentNode, parseType(parentNode))
  private fun varDecl(parentNode: CstNode?, type: TypeNode): VariableDeclarationNode {
    val identifierToken = accept(TokenType.IDENTIFIER)
    val expression = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
    return VariableDeclarationNode(type, identifierToken.value, expression, parentNode, type.tokenStart, expression?.tokenEnd ?: identifierToken)
  }

  private fun block(parentNode: CstNode?, acceptBracketOpen: Boolean = true): BlockNode {
    val token = current
    if (acceptBracketOpen) accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementNode>()
    while (current.type != TokenType.BRACKETS_CLOSE) {
      val statement = statement(parentNode)
      statements.add(statement)
    }
    skip() // skipping BRACKETS_CLOSE
    return BlockNode(statements, parentNode, token, previous)
  }

  fun expression(parentNode: CstNode? = null, maxPriority: Int = Int.MAX_VALUE): ExpressionNode {
    var a = atom(parentNode)
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      next()
      val leftOperand = a

      a = when (t.type) {
        TokenType.AS, TokenType.NOT_INSTANCEOF, TokenType.INSTANCEOF -> {
          val rightOperand = parseType(parentNode)
          BinaryTypeOperatorNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        }
        TokenType.QUESTION_MARK -> { // ternary
          val trueExpr = expression(parentNode)
          accept(TokenType.COLON)
          val falseExpr = expression(parentNode)
          TernaryNode(leftOperand, trueExpr, falseExpr, parentNode, leftOperand.tokenStart, falseExpr.tokenEnd)
        }
        else -> {
          val rightOperand = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
          BinaryOperatorNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        }
      }
      t = current
    }
    return a
  }

  fun atom(parentNode: CstNode? = null): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.INTEGER, TokenType.FLOAT -> {
        return parseNumberConstant(parentNode=parentNode, token=token)
      }
      TokenType.OPEN_QUOTE -> { // double quotes
        val parts = mutableListOf<ExpressionNode>()
        while (current.type != TokenType.CLOSING_QUOTE) {
          parts.add(stringPart(parentNode))
        }
        skip() // skip last quote
        TemplateStringNode(parts, parentNode, token, previous)
      }
      TokenType.OPEN_SIMPLE_QUOTE -> {
        val builder = StringBuilder()
        while (current.type != TokenType.CLOSING_SIMPLE_QUOTE) {
          builder.append(simpleStringPart())
        }
        skip() // skip last quote
        StringNode(parentNode, builder.toString(), token, previous)
      }
      TokenType.OPEN_REGEX_QUOTE -> {
        val builder = StringBuilder()
        while (current.type != TokenType.CLOSING_REGEX_QUOTE) {
          builder.append(simpleStringPart())
        }
        skip() // skip last quote
        val flags = mutableListOf<Int>()
        val optFlags = acceptOptional(TokenType.IDENTIFIER)?.value
        if (optFlags != null) {
          for (char in optFlags) {
            RegexNode.FLAGS_MAP
            flags.add(RegexNode.FLAGS_MAP[char] ?: throw MarcelParserException(
              previous,
              "Unknown pattern flag $char"
            )
            )
          }
        }
        RegexNode(parentNode, builder.toString(), flags, token, previous)
      }
      TokenType.NULL -> NullNode(parentNode, token)
      TokenType.AT -> {
        val referenceToken = accept(TokenType.IDENTIFIER)
        val node = DirectFieldReferenceNode(parentNode, referenceToken.value, referenceToken)
        optIndexAccessCstNode(parentNode, node) ?: node
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.INCR) IncrNode(parentNode, token.value, 1, true, token, next())
        else if (current.type == TokenType.DECR) IncrNode(parentNode, token.value, -1, true, token, next())
        else if (current.type == TokenType.LPAR
          // for funcCall<CastType>()
          || current.type == TokenType.LT && lookup(2)?.type == TokenType.GT && lookup(3)?.type == TokenType.LPAR) {
          var castType: TypeNode? = null
          if (current.type == TokenType.LT) {
            skip()
            castType = parseType(parentNode)
            accept(TokenType.GT)
          }
          skip()
          val (arguments, namedArguments) = parseFunctionArguments(parentNode)
          FunctionCallNode(parentNode, token.value, castType, arguments, namedArguments, token, previous)
        } else if (current.type == TokenType.BRACKETS_OPEN) { // function call with a lambda
          skip()
          FunctionCallNode(parentNode, token.value, null, listOf(parseLambda(token, parentNode)), emptyList(), token, previous)
        } else if (current.type == TokenType.DOT && lookup(1)?.type == TokenType.CLASS
          // for array class references
          || current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          val arrayDimensions = parseArrayDimensions()
          accept(TokenType.DOT)
          accept(TokenType.CLASS)
          ClassReferenceNode(parentNode, TypeNode(parentNode, token.value, emptyList(), arrayDimensions, token, previous), token, previous)
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
          indexAccessCstNode(parentNode, ReferenceNode(parentNode, token.value, token))
        } else {
          ReferenceNode(parentNode, token.value, token)
        }
      }
      TokenType.SQUARE_BRACKETS_OPEN -> {
        if (current.type == TokenType.COLON && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          skip()
          skip()
          return MapNode(emptyList(), parentNode, token, previous)
        }
        val elements = mutableListOf<ExpressionNode>()
        var isMap = false
        while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
          val isParenthesisBlock = current.type == TokenType.LPAR
          elements.add(expression(parentNode))
          if (current.type == TokenType.COLON) {
            val key = elements.last()
            if (!isParenthesisBlock && key is ReferenceNode) {
              elements[elements.lastIndex] = StringNode(parentNode, key.value, key.tokenStart, key.tokenEnd)
            }
            isMap = true
            skip()
            elements.add(expression(parentNode))
          }
          if (current.type == TokenType.COMMA) {
            skip()
          }
        }
        next() // skip square brackets close
        if (isMap) {
          val entries = mutableListOf<Pair<ExpressionNode, ExpressionNode>>()
          for (i in elements.indices step 2) {
            entries.add(Pair(elements[i], elements[i + 1]))
          }
          MapNode(entries, parentNode, token, previous)
        } else ArrayNode(elements, parentNode, token, previous)
      }
      TokenType.SUPER, TokenType.THIS -> {
        if (current.type == TokenType.LPAR) {
          skip()
          val (arguments, namedArguments) = parseFunctionArguments(parentNode, allowLambdaLastArg = false)
          if (namedArguments.isNotEmpty()) {
            throw MarcelParserException(
              token,
              "Cannot have named arguments on super constructor call"
            )
          }
          if (token.type == TokenType.SUPER) SuperConstructorCallNode(parentNode, arguments, token, arguments.lastOrNull()?.tokenEnd ?: token)
          else ThisConstructorCallNode(parentNode, arguments, token, arguments.lastOrNull()?.tokenEnd ?: token)
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
          indexAccessCstNode(parentNode, ThisReferenceNode(parentNode, token))
        } else {
          if (token.type == TokenType.SUPER) SuperReferenceNode(parentNode, token)
          else ThisReferenceNode(parentNode, token)
        }
      }
      TokenType.NEW -> {
        val type = parseType(parentNode)
        accept(TokenType.LPAR)
        val arguments = parseFunctionArguments(parentNode)
        NewInstanceNode(parentNode, type, arguments.first, arguments.second, token, previous)
      }
      TokenType.MINUS -> unaryOperator(parentNode, token, ::UnaryMinusNode)
      TokenType.NOT -> unaryOperator(parentNode, token, ::NotNode)
      TokenType.VALUE_TRUE -> BoolNode(parentNode, true, token)
      TokenType.VALUE_FALSE -> BoolNode(parentNode, false, token)
      TokenType.OPEN_CHAR_QUOTE -> {
        val valueToken = next()
        val value = when (valueToken.type) {
          TokenType.REGULAR_STRING_PART -> valueToken.value
          TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(valueToken.value)
          TokenType.END_OF_FILE -> throw eofException(token)
            else -> throw MarcelParserException(
            previous,
            "Unexpected token ${valueToken.type} for character constant"
          )
        }
        accept(TokenType.CLOSING_CHAR_QUOTE)
        if (value.length != 1) throw MarcelParserException(
          token,
          "Char constant can only contain one character"
        )
        CharNode(parentNode, value[0], token, previous)
      }
      TokenType.ESCAPE_SEQUENCE -> StringNode(parentNode, escapedSequenceValue(token.value), token, previous)
      TokenType.INCR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrNode(parentNode, identifierToken.value, 1, false, token, identifierToken)
      }
      TokenType.DECR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrNode(parentNode, identifierToken.value, -1, false, token, identifierToken)
      }
      TokenType.LPAR -> {
        val node = WrappedExpressionNode(expression(parentNode))
        if (current.type != TokenType.RPAR) {
          throw MarcelParserException(
            previous,
            "Parenthesis should be closed"
          )
        }
        next()
        node
      }
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE,
      TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID, TokenType.TYPE_CHAR -> {
        val arrayDimensions = parseArrayDimensions()
        accept(TokenType.DOT)
        accept(TokenType.CLASS)
        ClassReferenceNode(parentNode,
          TypeNode(parentNode, token.value, emptyList(), arrayDimensions, token, previous), token, previous)
      }
      TokenType.WHEN, TokenType.SWITCH -> {
        var switchExpression: ExpressionNode? = null
        var varDecl: VariableDeclarationNode? = null
        if (token.type == TokenType.SWITCH) {
          accept(TokenType.LPAR)
          if (ParserUtils.isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER && lookup(2)?.type == TokenType.ASSIGNMENT) {
            val type = parseType(parentNode)
            val varName = accept(TokenType.IDENTIFIER).value
            varDecl = VariableDeclarationNode(type, varName, null, parentNode, type.tokenStart, current)
            accept(TokenType.ASSIGNMENT)
          }
          switchExpression = expression(parentNode)
          accept(TokenType.RPAR)
        }
        accept(TokenType.BRACKETS_OPEN)
        val branches = mutableListOf<Pair<ExpressionNode, StatementNode>>()

        var elseStatement: StatementNode? = null
        while (current.type != TokenType.BRACKETS_CLOSE) {
          if (current.type == TokenType.ELSE) {
            skip()
            accept(TokenType.ARROW)
            if (elseStatement != null) throw MarcelParserException(
              previous,
              "Cannot have multiple else statements"
            )
            elseStatement = statement(parentNode)
          } else {
            val conditionExpressions = mutableListOf(expression(parentNode))
            while (current.type == TokenType.COMMA) {
              skip()
              conditionExpressions.add(expression(parentNode))
            }
            accept(TokenType.ARROW)
            val statement = statement(parentNode)
            conditionExpressions.forEach { conditionExpression ->
              branches.add(Pair(conditionExpression, statement))
            }
          }
        }
        skip() // skip bracket_close
        if (switchExpression == null) WhenNode(parentNode, token, previous, branches, elseStatement)
        else SwitchNode(parentNode, token, previous, branches, elseStatement, varDecl, switchExpression)
      }
      TokenType.BRACKETS_OPEN -> parseLambda(token, parentNode)
      else -> throw MarcelParserException(token, "Not supported $token")

    }
  }

  private inline fun unaryOperator(parentNode: CstNode?, token: LexToken,
                            nodeCreator: (ExpressionNode, CstNode?, LexToken, LexToken) -> ExpressionNode): ExpressionNode {
    val rootExpr = expression(parentNode, UNARY_PRIORITY)
    return if (rootExpr !is BinaryOperatorNode || ParserUtils.getPriority(rootExpr.tokenType) <= UNARY_PRIORITY) nodeCreator(rootExpr, parentNode, token, previous)
    else {
      var expr = rootExpr
      while (expr is BinaryOperatorNode
        && expr.leftOperand is BinaryOperatorNode
        && ParserUtils.getPriority((expr.leftOperand as BinaryOperatorNode).tokenType) > UNARY_PRIORITY) {
        expr = expr.leftOperand
      }
      expr as BinaryOperatorNode
      expr.leftOperand = nodeCreator(expr.leftOperand, parentNode, token, previous)
      rootExpr
    }
  }

  private fun stringPart(parentNode: CstNode?): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringNode(parentNode, token.value, token, token)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        ReferenceNode(parentNode, identifierToken.value, identifierToken)
      }
      TokenType.LONG_TEMPLATE_ENTRY_START -> {
        val expr = expression(parentNode)
        accept(TokenType.LONG_TEMPLATE_ENTRY_END)
        expr
      }
      else -> {
        rollback()
        expression(parentNode)
      }
    }
  }

  // BRACKETS_OPEN should have alreadt been parsed
  private fun parseLambda(token: LexToken, parentNode: CstNode?): LambdaNode {
    val parameters = mutableListOf<LambdaNode.MethodParameterCstNode>()
    var explicit0Parameters = false
    // first parameter with no type specified
    if (current.type == TokenType.IDENTIFIER && lookup(1)?.type in listOf(TokenType.COMMA, TokenType.ARROW, TokenType.LT) // LT for generic types
      || ParserUtils.isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER && lookup(2)?.type in listOf(TokenType.COMMA, TokenType.ARROW)) {
      while (current.type != TokenType.ARROW) {
        val firstToken = current
        val parameter = if (lookup(1)?.type == TokenType.IDENTIFIER || lookup(1)?.type == TokenType.LT) {
          val type = parseType(parentNode)
          val identifier = accept(TokenType.IDENTIFIER)
          LambdaNode.MethodParameterCstNode(parentNode, firstToken, identifier, type, identifier.value)
        } else {
          next()
          LambdaNode.MethodParameterCstNode(parentNode, firstToken, firstToken, null, firstToken.value)
        }
        parameters.add(parameter)
        if (current.type == TokenType.COMMA) skip()
      }
      skip() // skip arrow
    } else if (current.type == TokenType.ARROW) {
      explicit0Parameters = true
      skip() // skip arrow
    }
    // now parse function block
    val block = block(parentNode, acceptBracketOpen = false)
    return LambdaNode(parentNode, token, previous, parameters, block, explicit0Parameters)
  }


  private fun simpleStringPart(): String {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> token.value
      TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(token.value)
      TokenType.END_OF_FILE -> throw eofException(token)
      else -> throw MarcelParserException(
        token,
        "Illegal token ${token.type} when parsing literal string"
      )
    }
  }

  private fun escapedSequenceValue(tokenValue: String): String {
    return when (val escapedSequence = tokenValue.substring(1)) {
      "b" -> "\b"
      "n" -> "\n"
      "r" -> "\r"
      "t" -> "\t"
      "\\" -> "\\"
      "\'" -> "'"
      "\"" -> "\""
      "`" -> "`"
      "/" -> "/"
      else -> throw MarcelParserException(
        previous,
        "Unknown escaped sequence \\$escapedSequence"
      )
    }
  }


  // LPAR must be already parsed
  private fun parseFunctionArguments(parentNode: CstNode? = null, allowLambdaLastArg: Boolean = true): Pair<MutableList<ExpressionNode>, MutableList<Pair<String, ExpressionNode>>> {
    val arguments = mutableListOf<ExpressionNode>()
    val namedArguments = mutableListOf<Pair<String, ExpressionNode>>()
    while (current.type != TokenType.RPAR) {
      if (current.type == TokenType.IDENTIFIER && lookup(1)?.type == TokenType.COLON) {
        val identifierToken = accept(TokenType.IDENTIFIER)
        val name = identifierToken.value
        if (namedArguments.any { it.first == name }) {
          recordError("Method parameter $name was specified more than one", identifierToken)
        }
        accept(TokenType.COLON)
        namedArguments.add(Pair(name, expression(parentNode)))
      } else {
        if (namedArguments.isNotEmpty()) {
          recordError("Cannot have a positional function argument after a named one")
        }
        arguments.add(expression(parentNode))
      }

      if (current.type == TokenType.COMMA) {
        accept(TokenType.COMMA)
      }
    }
    skip() // skipping RPAR
    if (allowLambdaLastArg && current.type == TokenType.BRACKETS_OPEN) {
      // fcall ending with a lambda
      arguments.add(parseLambda(next(), null))
    }
    return Pair(arguments, namedArguments)
  }


  private fun indexAccessCstNode(parentNode: CstNode?, ownerNode: ExpressionNode): IndexAccessNode {
    val isSafeIndex = current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN
    val tokenStart = current
    skip()
    val indexArguments = mutableListOf<ExpressionNode>()
    while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
      indexArguments.add(expression(parentNode))
      if (current.type == TokenType.COMMA) skip()
    }
    skip() // skip brackets close
    return IndexAccessNode(parentNode, ownerNode, indexArguments, isSafeIndex, tokenStart, previous)
  }

  private fun optIndexAccessCstNode(parentNode: CstNode?, ownerNode: ExpressionNode): IndexAccessNode? {
    if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
      indexAccessCstNode(parentNode, ownerNode)
    }
    return null
  }

  private fun parseAnnotations(parentNode: CstNode?): List<AnnotationNode> {
    val annotations = mutableListOf<AnnotationNode>()
    while (current.type == TokenType.AT) {
      annotations.add(parseAnnotation(parentNode))
    }
    return annotations
  }

  private fun parseAnnotation(parentNode: CstNode?): AnnotationNode {
    val token = next()
    val type = parseType(parentNode)
    val attributes = mutableListOf<Pair<String, ExpressionNode>>()
    if (current.type == TokenType.LPAR) {
      skip()
      if (current.type == TokenType.IDENTIFIER && lookup(1)?.type == TokenType.ASSIGNMENT) {
        while (current.type != TokenType.RPAR) {
          val attributeName = accept(TokenType.IDENTIFIER).value
          accept(TokenType.ASSIGNMENT)
          val expression = expression(parentNode)
          attributes.add(Pair(attributeName, expression))
          if (current.type != TokenType.RPAR) accept(TokenType.COMMA)
        }
      } else {
        val expression = expression(parentNode)
        attributes.add(Pair("value", expression))
      }
      accept(TokenType.RPAR)
    }
    return AnnotationNode(parentNode, token, previous, type, attributes)
  }

  private fun parseNumberConstant(parentNode: CstNode? = null, token: LexToken): ExpressionNode {
    if (token.type == TokenType.INTEGER) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isLong = valueString.endsWith("l")
      if (isLong) valueString = valueString.substring(0, valueString.length - 1)

      val (radix, numberString) = if (valueString.startsWith("0x")) Pair(16, valueString.substring(2))
      else if (valueString.startsWith("0b")) Pair(2, valueString.substring(2))
      else Pair(10, valueString)

      return if (isLong) {
        val value = try {
          numberString.toLong(radix)
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0L
        }
        LongNode(parentNode, value, token)
      } else {
        val value = try {
          numberString.toInt(radix)
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0
        }
        IntNode(parentNode, value, token)
      }
    } else if (token.type == TokenType.FLOAT) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isDouble = valueString.endsWith("d")
      if (isDouble) valueString = valueString.substring(0, valueString.length - 1)

      return if (isDouble) {
        val value = try {
          valueString.toDouble()
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0.0
        }
        DoubleNode(parentNode, value, token)
      } else {
        val value = try {
          valueString.toFloat()
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0f
        }
        FloatNode(parentNode, value, token)
      }
    } else {
      throw MarcelParserException(
        token,
        "Unexpected token $token",
        eof
      )
    }
  }

  private fun accept(type: TokenType): LexToken {
    val token = current
    if (token.type != type) {
      throw MarcelParserException(
        current, "Expected token of type $type but got ${token.type}"
      )
    }
    currentIndex++
    return token
  }

  private fun acceptOneOf(vararg types: TokenType): LexToken {
    val token = current
    if (token.type !in types) {
      throw MarcelParserException(
        current,
        "Expected token of type ${types.contentToString()} but got ${token.type}"
      )
    }
    currentIndex++
    return token
  }

  private fun acceptOptionalOneOf(vararg types: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type in types) {
      currentIndex++
      return token
    }
    return null
  }

  private fun acceptOptional(t: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type == t) {
      currentIndex++
      return token
    } else {
      return null
    }
  }

  private fun recordError(message: String, token: LexToken = current) {
    recordError(MarcelParserException.error(message, eof, token))
  }
  private fun recordError(error: MarcelParserException.Error) {
    errors.add(error)
  }

  private fun rollback() {
    currentIndex--
  }

  private fun skip() {
    currentIndex++
  }

  private fun skip(howMuch: Int) {
    currentIndex+= howMuch
  }

  private fun next(): LexToken {
    checkEof()
    return tokens[currentIndex++]
  }

  private fun lookup(howFar: Int): LexToken? {
    return tokens.getOrNull(currentIndex + howFar)
  }

  private fun checkEof() {
    if (eof) {
      throw MarcelParserException(
        currentSafe ?: previous,
        "Unexpected end of file",
        true
      )
    }
  }

  private fun eofException(token: LexToken = current): MarcelParserException {
    return MarcelParserException(
      token,
      "Unexpected end of file",
      true
    )
  }
}