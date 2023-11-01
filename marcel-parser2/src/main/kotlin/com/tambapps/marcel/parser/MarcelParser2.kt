package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AbstractMethodNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.CstAccessNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.expression.reference.*
import com.tambapps.marcel.parser.cst.imprt.ImportCstNode
import com.tambapps.marcel.parser.cst.imprt.SimpleImportCstNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportCstNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfCstStatementNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode
import java.lang.NumberFormatException
import java.util.*

class MarcelParser2 constructor(private val classSimpleName: String, tokens: List<LexToken>) {

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

  private val errors = mutableListOf<MarcelParser2Exception.Error>()

  fun parse(): SourceFileCstNode {
    if (tokens.isEmpty()) {
      throw MarcelParser2Exception(LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, null), "Unexpected end of file")
    }
    val packageName = parsePackage()

    val sourceFile = SourceFileCstNode(packageName = packageName, tokenStart = tokens.first(), tokenEnd = tokens.last())
    val scriptNode = ScriptCstNode(tokens.first(), tokens.last(),
      if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName)

    val (imports, extensionTypes) = parseImports(sourceFile)
    sourceFile.imports.addAll(imports)
    sourceFile.extensionTypes.addAll(extensionTypes)

    while (current.type != TokenType.END_OF_FILE) {
      parseMember(packageName, sourceFile, scriptNode, null)
    }

    // class defined are not script class inner classes
    sourceFile.classes.addAll(scriptNode.innerClasses)
    scriptNode.innerClasses.clear()

    if (scriptNode.isNotEmpty) sourceFile.script = scriptNode
    if (errors.isNotEmpty()) {
      throw MarcelParser2Exception(errors)
    }
    return sourceFile
  }
  private fun parseMember(packageName: String?, parentNode: CstNode?, classCstNode: ClassCstNode, outerClassNode: ClassCstNode?) {
    val annotations = parseAnnotations(parentNode)
    val access = parseAccess(parentNode)
    if (current.type == TokenType.CLASS) {
      classCstNode.innerClasses.add(parseClass(packageName, parentNode, annotations, access, outerClassNode))
    } else if (current.type == TokenType.FUN || current.type == TokenType.CONSTRUCTOR) {
      when (val method = method(parentNode, annotations, access)) {
        is MethodCstNode -> classCstNode.methods.add(method)
        is ConstructorCstNode -> classCstNode.constructors.add(method)
      }
    } else if (annotations.isNotEmpty() || access.isExplicit) {
      // it must be a field
      classCstNode.fields.add(field(parentNode, annotations, access))
    } else if (classCstNode is ScriptCstNode) {
      classCstNode.runMethodStatements.add(statement(parentNode))
    } else throw MarcelParser2Exception(current, "Illegal node " + current.type)
  }

  private fun parseImports(parentNode: CstNode?): Pair<List<ImportCstNode>, List<TypeCstNode>>  {
    val imports = mutableListOf<ImportCstNode>()
    val extensionTypes = mutableListOf<TypeCstNode>()
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

  fun import(parentNode: CstNode?): ImportCstNode {
    val importToken = accept(TokenType.IMPORT)
    val staticImport = acceptOptional(TokenType.STATIC) != null
    val classParts = mutableListOf(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      if (current.type == TokenType.MUL) {
        if (staticImport) {
          throw MarcelParser2Exception(
            current,
            "Invalid static import"
          )
        }
        skip()
        acceptOptional(TokenType.SEMI_COLON)
        return WildcardImportCstNode(parentNode, importToken, previous, classParts.joinToString(separator = "."))
      }
      classParts.add(accept(TokenType.IDENTIFIER).value)
    }
    if (classParts.size <= 1) {
      throw MarcelParser2Exception(
        previous,
        "Invalid class full name" + classParts.joinToString(
          separator = "."
        )
      )
    }
    val node = if (staticImport) {
      val className = classParts.subList(0, classParts.size - 1).joinToString(separator = ".")
      val method = classParts.last()
      StaticImportCstNode(parentNode, importToken, previous, className, method)
    } else {
      val asName = if (acceptOptional(TokenType.AS) != null) accept(TokenType.IDENTIFIER).value else null
      SimpleImportCstNode(parentNode, importToken, previous, classParts.joinToString(separator = "."), asName)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return node
  }

  private fun parsePackage(): String? {
    if (acceptOptional(TokenType.PACKAGE) == null) return null
    val parts = mutableListOf<String>(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      parts.add(accept(TokenType.IDENTIFIER).value)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return parts.joinToString(separator = ".")
  }

  fun field(parentNode: CstNode?, annotations: List<AnnotationCstNode>, access: CstAccessNode): FieldCstNode {
    val tokenStart = current
    val type = parseType(parentNode)
    val name = accept(TokenType.IDENTIFIER).value
    val initialValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
    return FieldCstNode(parentNode, tokenStart, previous, access, annotations, type, name, initialValue)
  }

  private fun parseClass(packageName: String?, parentNode: CstNode?, annotations: List<AnnotationCstNode>, access: CstAccessNode,
                         outerClassNode: ClassCstNode? = null): ClassCstNode {
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
    val interfaces = mutableListOf<TypeCstNode>()
    if (acceptOptional(TokenType.IMPLEMENTS) != null) {
      while (current.type == TokenType.IDENTIFIER) {
        interfaces.add(parseType(parentNode))
        acceptOptional(TokenType.COMMA)
      }
    }
    val classCstNode = ClassCstNode(classToken, classToken, access, className, superType, interfaces, forExtensionClassType)
    classCstNode.annotations.addAll(annotations)

    accept(TokenType.BRACKETS_OPEN)
    while (current.type != TokenType.BRACKETS_CLOSE) {
      parseMember(packageName, classCstNode, classCstNode, classCstNode)
    }
    accept(TokenType.BRACKETS_CLOSE)
    return classCstNode
  }

  fun method(parentNode: CstNode?, annotations: List<AnnotationCstNode>, access: CstAccessNode): AbstractMethodNode {
    val token = current
    val isConstructor = accept(TokenType.FUN, TokenType.CONSTRUCTOR).type == TokenType.CONSTRUCTOR
    val node = if (!isConstructor) {
      val returnType = parseType(parentNode)
      val methodName = accept(TokenType.IDENTIFIER).value
      MethodCstNode(parentNode, token, token, access, methodName, returnType)
    } else ConstructorCstNode(parentNode, token, token, access)
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
        else TypeCstNode(parentNode, "", emptyList(), 0, previous, previous)
      val parameterName = accept(TokenType.IDENTIFIER).value
      val defaultValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
      parameters.add(MethodParameterCstNode(parentNode, parameterTokenStart, previous, parameterName, type, defaultValue, parameterAnnotations, isThisParameter))
      acceptOptional(TokenType.COMMA)
    }
    skip() // skip RPAR

    val statements = mutableListOf<StatementCstNode>()
    // super/this method call if any
    if (isConstructor && current.type == TokenType.COLON) {
      skip()
      when (val atom = atom(parentNode)) {
        // TODO handle thisConstructorCall
        is SuperConstructorCallCstNode  -> statements.add(ExpressionStatementCstNode(atom))
        else -> throw MarcelParser2Exception(atom.token, "Expected this or super constructor call")
      }
    }

    if (isConstructor && current.type != TokenType.BRACKETS_OPEN) {
      // constructor may not have a body
      node.statements.addAll(statements)
      return node
    }

    if (current.type == TokenType.ARROW) {
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

  private fun parseAccess(parentNode: CstNode?): CstAccessNode {
    val startIndex = currentIndex
    val tokenStart = current
    val visibilityToken = acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
      TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC
    val isStatic = acceptOptional(TokenType.STATIC) != null
    val isFinal = acceptOptional(TokenType.FINAL) != null
    val isInline = acceptOptional(TokenType.INLINE) != null
    val hasExplicitAccess = currentIndex > startIndex
    return CstAccessNode(parentNode, tokenStart, if (hasExplicitAccess) previous else current, isStatic, isInline, isFinal, visibilityToken, hasExplicitAccess)
  }

  internal fun parseType(parentNode: CstNode? = null): TypeCstNode {
    val tokenStart = next()
    val typeFragments = mutableListOf<String>(
      parseTypeFragment(tokenStart)
    )
    while (current.type == TokenType.DOT) {
      typeFragments.add(parseTypeFragment(next()))
    }

    // generic types
    val genericTypes = mutableListOf<TypeCstNode>()
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
    return TypeCstNode(parentNode, typeFragments.joinToString(separator = "."), genericTypes, arrayDimension, tokenStart, previous)
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
    TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_SHORT, TokenType.IDENTIFIER -> token.value
    TokenType.DYNOBJ -> "DynamicObject"
    else -> throw MarcelParser2Exception(token, "Doesn't handle type ${token.type}") // TODO this error can be a non fatal one
  }

  fun statement(parentNode: CstNode? = null): StatementCstNode {
    val token = next()
    try {
      return when (token.type) {
        TokenType.RETURN -> {
          val expr = expression(parentNode)
          ReturnCstNode(parentNode, expr, expr.tokenStart, expr.tokenEnd)
        }
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT,
        TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID,
        TokenType.TYPE_CHAR, TokenType.DYNOBJ -> {
          rollback()
          varDecl(parentNode)
        }
        TokenType.IDENTIFIER -> {
          if (current.type == TokenType.IDENTIFIER && lookup(1)?.type == TokenType.ASSIGNMENT
            || current.type == TokenType.LT)  {
            rollback()
            varDecl(parentNode)
          } else {
            rollback()
            val expr = expression(parentNode)
            ExpressionStatementCstNode(parentNode, expr, expr.tokenStart, expr.tokenEnd)
          }
        }
        TokenType.BRACKETS_OPEN -> block(parentNode, acceptBracketOpen = false)
        TokenType.IF -> {
          accept(TokenType.LPAR)
          val condition = ifConditionExpression(parentNode)
          accept(TokenType.RPAR)
          val rootIf = IfCstStatementNode(condition, statement(parentNode), null, parentNode, token, previous)
          var currentIf = rootIf
          while (current.type == TokenType.ELSE) {
            skip()
            if (acceptOptional(TokenType.IF) != null) {
              accept(TokenType.LPAR)
              val elseIfCondition = ifConditionExpression(parentNode)
              accept(TokenType.RPAR)
              val newIf = IfCstStatementNode(elseIfCondition, statement(parentNode), null, parentNode, token, previous)
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
          WhileCstNode(parentNode, token, statement.tokenEnd, condition, statement)
        }
        TokenType.THROW -> {
          val expression = expression(parentNode)
          ThrowCstNode(parentNode, token, previous, expression)
        }
        TokenType.DEF -> {
          accept(TokenType.LPAR)
          val declarations = mutableListOf<Pair<TypeCstNode, String>?>()
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
          MultiVarDeclarationCstNode(parentNode, token, previous, declarations, expression)
        }
        TokenType.FOR -> {
          accept(TokenType.LPAR)
          if (lookup(1)?.type == TokenType.IDENTIFIER && lookup(2)?.type == TokenType.IN) {
            // for in statement
            val type = parseType(parentNode)
            val identifier = accept(TokenType.IDENTIFIER).value
            accept(TokenType.IN)
            val expression = expression(parentNode)
            accept(TokenType.RPAR)
            val forBlock = statement(parentNode)
            ForInCstNode(type, identifier, expression, forBlock, parentNode, token, previous)
          } else {
            // for (;;)
            // needed especially if initStatement is var declaration
            val initStatement = statement(parentNode)
            if (initStatement !is VariableDeclarationCstNode) {
              throw MarcelParser2Exception(
                previous,
                "For loops should start with variable declaration/assignment"
              )
            }
            accept(TokenType.SEMI_COLON)
            val condition = expression(parentNode)
            accept(TokenType.SEMI_COLON)
            val iteratorStatement = statement(parentNode)
            accept(TokenType.RPAR)
            val forBlock = statement(parentNode)
            ForVarCstNode(initStatement, condition, iteratorStatement, forBlock, parentNode, token, forBlock.tokenEnd)
          }
        }
        TokenType.BREAK -> BreakCstNode(parentNode, token)
        TokenType.CONTINUE -> ContinueCstNode(parentNode, token)
        TokenType.TRY -> {
          val resources = mutableListOf<VariableDeclarationCstNode>()
          if (current.type == TokenType.LPAR) {
            skip()
            while (current.type != TokenType.RPAR) {
              val e = statement(parentNode)
              if (e !is VariableDeclarationCstNode) throw MarcelParser2Exception(e.token, "Can only declare variables in try with resources")
              resources.add(e)
              if (current.type == TokenType.COMMA) skip()
            }
            skip() // skip RPAR
          }
          val tryNode = statement(parentNode)
          val catchNodes = mutableListOf<Triple<List<TypeCstNode>, String, StatementCstNode>>()
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
          TryCatchCstNode(parentNode, token, previous, tryNode, resources, catchNodes, finallyBlock)
        }
        else -> {
          rollback()
          val expr = expression(parentNode)
          ExpressionStatementCstNode(parentNode, expr, expr.tokenStart, expr.tokenEnd)
        }
      }
    } finally {
      acceptOptional(TokenType.SEMI_COLON)
    }
 }

  private fun ifConditionExpression(parentNode: CstNode?): ExpressionCstNode {
    return if (ParserUtils.isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER) {
      val type = parseType(parentNode)
      val variableName = accept(TokenType.IDENTIFIER).value
      accept(TokenType.ASSIGNMENT)
      val expression = expression(parentNode)
      TruthyVariableDeclarationCstNode(parentNode, type.tokenStart, expression.tokenEnd, type, variableName, expression)
    } else expression(parentNode)
  }

  private fun varDecl(parentNode: CstNode?): VariableDeclarationCstNode {
    val type = parseType(parentNode)
    val identifierToken = accept(TokenType.IDENTIFIER)
    val expression = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
    return VariableDeclarationCstNode(type, identifierToken.value, expression, parentNode, type.tokenStart, expression?.tokenEnd ?: identifierToken)
  }

  private fun block(parentNode: CstNode?, acceptBracketOpen: Boolean = true): BlockCstNode {
    val token = current
    if (acceptBracketOpen) accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementCstNode>()
    while (current.type != TokenType.BRACKETS_CLOSE) {
      val statement = statement(parentNode)
      statements.add(statement)
    }
    skip() // skipping BRACKETS_CLOSE
    return BlockCstNode(statements, parentNode, token, previous)
  }

  fun expression(parentNode: CstNode? = null): ExpressionCstNode {
    val expression = expression(parentNode, Int.MAX_VALUE)
    if (current.type != TokenType.QUESTION_MARK) {
      return expression
    }
    skip()
    val trueExpression = expression(parentNode)
    accept(TokenType.COLON)
    val falseExpression = expression(parentNode)
    return TernaryCstNode(expression, trueExpression, falseExpression, parentNode, expression.tokenStart, falseExpression.tokenEnd)
  }

  private fun expression(parentNode: CstNode?, maxPriority: Int): ExpressionCstNode {
    var a = atom(parentNode)
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      next()
      val leftOperand = a

      a = when (t.type) {
        TokenType.AS, TokenType.NOT_INSTANCEOF, TokenType.INSTANCEOF -> {
          val rightOperand = parseType(parentNode)
          BinaryTypeOperatorCstNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        }
        else -> {
          val rightOperand = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
          BinaryOperatorCstNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        }
      }
      t = current
    }
    return a
  }

  fun atom(parentNode: CstNode? = null): ExpressionCstNode {
    val token = next()
    return when (token.type) {
      TokenType.INTEGER, TokenType.FLOAT -> {
        return parseNumberConstant(parentNode=parentNode, token=token)
      }
      TokenType.OPEN_QUOTE -> { // double quotes
        val parts = mutableListOf<ExpressionCstNode>()
        while (current.type != TokenType.CLOSING_QUOTE) {
          parts.add(stringPart(parentNode))
        }
        skip() // skip last quote
        TemplateStringCstNode(parts, parentNode, token, previous)
      }
      TokenType.OPEN_SIMPLE_QUOTE -> {
        val builder = StringBuilder()
        while (current.type != TokenType.CLOSING_SIMPLE_QUOTE) {
          builder.append(simpleStringPart())
        }
        skip() // skip last quote
        StringCstNode(parentNode, builder.toString(), token, previous)
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
            RegexCstNode.FLAGS_MAP
            flags.add(RegexCstNode.FLAGS_MAP[char] ?: throw MarcelParser2Exception(
              previous,
              "Unknown pattern flag $char"
            )
            )
          }
        }
        RegexCstNode(parentNode, builder.toString(), flags, token, previous)
      }
      TokenType.NULL -> NullCstNode(parentNode, token)
      TokenType.AT -> {
        val referenceToken = accept(TokenType.IDENTIFIER)
        val node = DirectFieldReferenceCstNode(parentNode, referenceToken.value, referenceToken)
        optIndexAccessCstNode(parentNode, node) ?: node
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.INCR) IncrCstNode(parentNode, token.value, 1, true, token, next())
        else if (current.type == TokenType.DECR) IncrCstNode(parentNode, token.value, -1, true, token, next())
        else if (current.type == TokenType.LPAR
          // for funcCall<CastType>()
          || current.type == TokenType.LT && lookup(2)?.type == TokenType.GT && lookup(3)?.type == TokenType.LPAR) {
          var castType: TypeCstNode? = null
          if (current.type == TokenType.LT) {
            skip()
            castType = parseType(parentNode)
            accept(TokenType.GT)
          }
          skip()
          val (arguments, namedArguments) = parseFunctionArguments(parentNode)
          FunctionCallCstNode(parentNode, token.value, castType, arguments, namedArguments, token, previous)
        } else if (current.type == TokenType.BRACKETS_OPEN) { // function call with a lambda
          skip()
          FunctionCallCstNode(parentNode, token.value, null, listOf(TODO("parse lambda")), emptyList(), token, previous)
        } else if (current.type == TokenType.DOT && lookup(1)?.type == TokenType.CLASS
          // for array class references
          || current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          val arrayDimensions = parseArrayDimensions()
          accept(TokenType.DOT)
          accept(TokenType.CLASS)
          ClassReferenceCstNode(parentNode, TypeCstNode(parentNode, token.value, emptyList(), arrayDimensions, token, previous), token, previous)
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
          indexAccessCstNode(parentNode, ReferenceCstNode(parentNode, token.value, token))
        } else {
          ReferenceCstNode(parentNode, token.value, token)
        }
      }
      TokenType.SQUARE_BRACKETS_OPEN -> {
        if (current.type == TokenType.COLON && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          skip()
          skip()
          return MapCstNode(emptyList(), parentNode, token, previous)
        }
        val elements = mutableListOf<ExpressionCstNode>()
        var isMap = false
        while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
          val isParenthesisBlock = current.type == TokenType.LPAR
          elements.add(expression(parentNode))
          if (current.type == TokenType.COLON) {
            val key = elements.last()
            if (!isParenthesisBlock && key is ReferenceCstNode) {
              elements[elements.lastIndex] = StringCstNode(parentNode, key.value, key.tokenStart, key.tokenEnd)
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
          val entries = mutableListOf<Pair<ExpressionCstNode, ExpressionCstNode>>()
          for (i in elements.indices step 2) {
            entries.add(Pair(elements[i], elements[i + 1]))
          }
          MapCstNode(entries, parentNode, token, previous)
        } else ArrayCstNode(elements, parentNode, token, previous)
      }
      TokenType.SUPER -> {
        if (current.type == TokenType.LPAR) {
          skip()
          val (arguments, namedArguments) = parseFunctionArguments(parentNode)
          if (namedArguments.isNotEmpty()) {
            throw MarcelParser2Exception(token, "Cannot have named arguments on super constructor call")
          }
          SuperConstructorCallCstNode(parentNode, arguments, token, arguments.lastOrNull()?.tokenEnd ?: token)
        } else SuperReferenceCstNode(parentNode, token)
      }
      TokenType.NEW -> {
        val type = parseType(parentNode)
        accept(TokenType.LPAR)
        val arguments = parseFunctionArguments(parentNode)
        NewInstanceCstNode(parentNode, type, arguments.first, arguments.second, token, previous)
      }
      TokenType.MINUS -> UnaryMinusCstNode(expression(parentNode), parentNode, token, previous)
      TokenType.NOT -> NotCstNode(expression(parentNode), parentNode, token, previous)
      TokenType.VALUE_TRUE -> BoolCstNode(parentNode, true, token)
      TokenType.VALUE_FALSE -> BoolCstNode(parentNode, false, token)
      TokenType.OPEN_CHAR_QUOTE -> {
        val valueToken = next()
        val value = when (valueToken.type) {
          TokenType.REGULAR_STRING_PART -> valueToken.value
          TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(valueToken.value)
          TokenType.END_OF_FILE -> throw MarcelParser2Exception(
            token,
            "Unexpected end of file",
            true
          )
          else -> throw MarcelParser2Exception(
            previous,
            "Unexpected token ${valueToken.type} for character constant"
          )
        }
        accept(TokenType.CLOSING_CHAR_QUOTE)
        if (value.length != 1) throw MarcelParser2Exception(token, "Char constant can only contain one character")
        CharCstNode(parentNode, value[0], token, previous)
      }
      TokenType.ESCAPE_SEQUENCE -> StringCstNode(parentNode, escapedSequenceValue(token.value), token, previous)
      TokenType.INCR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrCstNode(parentNode, identifierToken.value, 1, false, token, identifierToken)
      }
      TokenType.DECR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrCstNode(parentNode, identifierToken.value, -1, false, token, identifierToken)
      }
      TokenType.LPAR -> {
        val node = expression(parentNode)
        if (current.type != TokenType.RPAR) {
          throw MarcelParser2Exception(
            previous,
            "Parenthesis should be close"
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
        ClassReferenceCstNode(parentNode,
          TypeCstNode(parentNode, token.value, emptyList(), arrayDimensions, token, previous), token, previous)
      }
      TokenType.WHEN, TokenType.SWITCH -> {
        var switchExpression: ExpressionCstNode? = null
        if (token.type == TokenType.SWITCH) {
          accept(TokenType.LPAR)
          switchExpression = expression(parentNode)
          accept(TokenType.RPAR)
        }
        accept(TokenType.BRACKETS_OPEN)
        val branches = mutableListOf<Pair<ExpressionCstNode, StatementCstNode>>()

        var elseStatement: StatementCstNode? = null
        while (current.type != TokenType.BRACKETS_CLOSE) {
          if (current.type == TokenType.ELSE) {
            skip()
            accept(TokenType.ARROW)
            if (elseStatement != null) throw MarcelParser2Exception(
              previous,
              "Cannot have multiple else statements"
            )
            elseStatement = statement(parentNode)
          } else {
            val conditionExpression = expression(parentNode)
            accept(TokenType.ARROW)
            branches.add(Pair(conditionExpression, statement(parentNode)))
          }
        }
        skip() // skip bracket_close
        if (switchExpression == null) WhenCstNode(parentNode, token, previous, branches, elseStatement)
        else SwitchCstNode(parentNode, token, previous, branches, elseStatement, switchExpression)
      }
      else -> TODO("atom  ${token.type} l:${token.line} c:${token.column}")
    }
  }

  private fun stringPart(parentNode: CstNode?): ExpressionCstNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringCstNode(parentNode, token.value, token, token)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        ReferenceCstNode(parentNode, identifierToken.value, identifierToken)
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

  private fun simpleStringPart(): String {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> token.value
      TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(token.value)
      TokenType.END_OF_FILE -> throw MarcelParser2Exception(
        token,
        "Unexpected end of file",
        true
      )
      else -> throw MarcelParser2Exception(
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
      else -> throw MarcelParser2Exception(
        previous,
        "Unknown escaped sequence \\$escapedSequence"
      )
    }
  }


  // LPAR must be already parsed
  private fun parseFunctionArguments(parentNode: CstNode? = null): Pair<MutableList<ExpressionCstNode>, MutableList<Pair<String, ExpressionCstNode>>> {
    val arguments = mutableListOf<ExpressionCstNode>()
    val namedArguments = mutableListOf<Pair<String, ExpressionCstNode>>()
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
    return Pair(arguments, namedArguments)
  }


  private fun indexAccessCstNode(parentNode: CstNode?, ownerNode: ExpressionCstNode): IndexAccessCstNode {
    val isSafeIndex = current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN
    val tokenStart = current
    skip()
    val indexArguments = mutableListOf<ExpressionCstNode>()
    while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
      indexArguments.add(expression(parentNode))
      if (current.type == TokenType.COMMA) skip()
    }
    skip() // skip brackets close
    return IndexAccessCstNode(parentNode, ownerNode, indexArguments, isSafeIndex, tokenStart, previous)
  }

  private fun optIndexAccessCstNode(parentNode: CstNode?, ownerNode: ExpressionCstNode): IndexAccessCstNode? {
    if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
      indexAccessCstNode(parentNode, ownerNode)
    }
    return null
  }

  private fun parseAnnotations(parentNode: CstNode?): List<AnnotationCstNode> {
    val annotations = mutableListOf<AnnotationCstNode>()
    while (current.type == TokenType.AT) {
      annotations.add(parseAnnotation(parentNode))
    }
    return annotations
  }

  private fun parseAnnotation(parentNode: CstNode?): AnnotationCstNode {
    val token = next()
    val type = parseType(parentNode)
    val attributes = mutableListOf<Pair<String, ExpressionCstNode>>()
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
    return AnnotationCstNode(parentNode, token, previous, type, attributes)
  }

  private fun parseNumberConstant(parentNode: CstNode? = null, token: LexToken): ExpressionCstNode {
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
          recordError(MarcelParser2Exception.malformedNumber(e, token, eof))
          0L
        }
        LongCstNode(parentNode, value, token)
      } else {
        val value = try {
          numberString.toInt(radix)
        } catch (e: NumberFormatException) {
          recordError(MarcelParser2Exception.malformedNumber(e, token, eof))
          0
        }
        IntCstNode(parentNode, value, token)
      }
    } else if (token.type == TokenType.FLOAT) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isDouble = valueString.endsWith("d")
      if (isDouble) valueString = valueString.substring(0, valueString.length - 1)

      return if (isDouble) {
        val value = try {
          valueString.toDouble()
        } catch (e: NumberFormatException) {
          recordError(MarcelParser2Exception.malformedNumber(e, token, eof))
          0.0
        }
        DoubleCstNode(parentNode, value, token)
      } else {
        val value = try {
          valueString.toFloat()
        } catch (e: NumberFormatException) {
          recordError(MarcelParser2Exception.malformedNumber(e, token, eof))
          0f
        }
        FloatCstNode(parentNode, value, token)
      }
    } else {
      throw MarcelParser2Exception(
        token,
        "Unexpected token $token",
        eof
      )
    }
  }

  private fun accept(vararg types: TokenType): LexToken {
    val token = current
    if (token.type !in types) {
      throw MarcelParser2Exception(
        current,
        "Expected token of type ${types.contentToString()} but got ${token.type}"
      )
    }
    currentIndex++
    return token
  }

  private fun acceptOptional(vararg types: TokenType): LexToken? {
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
    recordError(MarcelParser2Exception.error(message, eof, token))
  }
  private fun recordError(error: MarcelParser2Exception.Error) {
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
  fun reset() {
    currentIndex = 0
  }

  private fun checkEof() {
    if (eof) {
      throw MarcelParser2Exception(
        currentSafe ?: previous,
        "Unexpected end of file",
        true
      )
    }
  }
}