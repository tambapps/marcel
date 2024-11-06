package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.lexer.TokenType.BRACKETS_OPEN
import com.tambapps.marcel.lexer.TokenType.DYNOBJ
import com.tambapps.marcel.lexer.TokenType.END_OF_FILE
import com.tambapps.marcel.lexer.TokenType.FLOAT
import com.tambapps.marcel.lexer.TokenType.FOR
import com.tambapps.marcel.lexer.TokenType.IDENTIFIER
import com.tambapps.marcel.lexer.TokenType.INTEGER
import com.tambapps.marcel.lexer.TokenType.LINE_RETURN
import com.tambapps.marcel.lexer.TokenType.NEW
import com.tambapps.marcel.lexer.TokenType.OPEN_CHAR_QUOTE
import com.tambapps.marcel.lexer.TokenType.OPEN_QUOTE
import com.tambapps.marcel.lexer.TokenType.OPEN_REGEX_QUOTE
import com.tambapps.marcel.lexer.TokenType.OPEN_SIMPLE_QUOTE
import com.tambapps.marcel.lexer.TokenType.RPAR
import com.tambapps.marcel.lexer.TokenType.SEMI_COLON
import com.tambapps.marcel.lexer.TokenType.SQUARE_BRACKETS_OPEN
import com.tambapps.marcel.lexer.TokenType.THREE_DOTS
import com.tambapps.marcel.lexer.TokenType.VALUE_FALSE
import com.tambapps.marcel.lexer.TokenType.VALUE_TRUE
import com.tambapps.marcel.lexer.TokenType.WHITESPACE
import com.tambapps.marcel.parser.ParserUtils.UNARY_PRIORITY
import com.tambapps.marcel.parser.cst.AbstractMethodCstNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.EnumCstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.AllInCstNode
import com.tambapps.marcel.parser.cst.expression.AnyInCstNode
import com.tambapps.marcel.parser.cst.expression.MapFilterCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
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
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.FindInCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
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
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode
import marcel.lang.DynamicObject
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

/**
 * The official Parser of Marcel Lang
 *
 * @property classSimpleName the class simple name to use for scripts
 * @constructor creates a MarcelParser instance
 *
 * @param tokens the list of [tokens][LexToken] returned by the [Lexer][com.tambapps.marcel.lexer.MarcelLexer]
 */
class MarcelParser constructor(private val classSimpleName: String, tokens: List<LexToken>) {

  companion object {
    private val FCALL_WITHOUT_PARENTHESIS_ALLOWED_TOKENS: Set<TokenType> = EnumSet.copyOf(listOf(
      IDENTIFIER, OPEN_QUOTE, OPEN_CHAR_QUOTE, OPEN_REGEX_QUOTE, OPEN_SIMPLE_QUOTE, SQUARE_BRACKETS_OPEN, NEW,
      INTEGER, FLOAT, VALUE_TRUE, VALUE_FALSE
    ))
  }
  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens)

  private val tokens = tokens.filter { it.type != WHITESPACE && !MarcelLexer.isCommentToken(it) }

  private var currentIndex = 0

  init {
    while (currentIndex < this.tokens.size && this.tokens[currentIndex].type == LINE_RETURN) currentIndex++
  }

  private val current: LexToken get() {
    checkEof()
    return tokens[currentIndex]
  }

  private val wasEndOfLine get() = tokens[currentIndex - 1].type == LINE_RETURN

  private val previous: LexToken get() {
    var currentIndex = this.currentIndex
    currentIndex--
    while (currentIndex >= 0 && tokens[currentIndex].type == LINE_RETURN) currentIndex--
    return tokens[currentIndex]
  }

  private val eof: Boolean
    get() = currentIndex >= tokens.size
  private val currentSafe: LexToken?
    get() = if (eof) null else tokens[currentIndex]

  private val errors = mutableListOf<MarcelParserException.Error>()


  /**
   * Parse the list of tokens to transform them into a Concrete Syntax Tree
   *
   * @return the resulting CST
   */
  fun parse(): SourceFileCstNode {
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

    val dumbbells = dumbbells()

    val sourceFile = SourceFileCstNode(packageName = packageName, tokenStart = tokens.first(),
      dumbbells = dumbbells,
      tokenEnd = tokens.last())
    val scriptNode = ScriptCstNode(sourceFile, tokens.first(), tokens.last(),
      if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName)

    val (imports, extensionTypes) = parseImports(sourceFile)
    sourceFile.imports.addAll(imports)
    sourceFile.extensionImports.addAll(extensionTypes)

    while (current.type != TokenType.END_OF_FILE) {
      parseMember(sourceFile, packageName, sourceFile, scriptNode, null)
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
  private fun parseMember(
    sourceFile: SourceFileCstNode,
    packageName: String?,
    parentNode: CstNode?,
    classNode: ClassCstNode,
    outerClassNode: ClassCstNode?
  ) {
    val annotations = parseAnnotations(parentNode)
    val access = parseAccess(parentNode)
    if (current.type == TokenType.CLASS || current.type == TokenType.EXTENSION) {
      classNode.innerClasses.add(parseClass(sourceFile, packageName, parentNode, annotations, access, outerClassNode))
    } else if (current.type == TokenType.FUN || current.type == TokenType.CONSTRUCTOR || current.type == TokenType.ASYNC && lookup(1)?.type == TokenType.FUN) {
      when (val method = method(classNode, annotations, access)) {
        is MethodCstNode -> classNode.methods.add(method)
        is ConstructorCstNode -> classNode.constructors.add(method)
      }
    } else if (current.type == TokenType.ENUM) {
      classNode.innerClasses.add(parseEnum(sourceFile, packageName, parentNode, annotations, access, outerClassNode))
    } else if (classNode is ScriptCstNode) {
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

  private fun parseImports(parentNode: CstNode?): Pair<List<ImportCstNode>, List<TypeCstNode>>  {
    val imports = mutableListOf<ImportCstNode>()
    val extensionTypes = mutableListOf<TypeCstNode>()
    while (current.type == TokenType.IMPORT) {
      if (lookup(1)?.type == TokenType.EXTENSION) {
        skip(2)
        val tokenStart = current
        extensionTypes.add(
          TypeCstNode(parentNode, importExtensionType(), emptyList(), 0, tokenStart, previous)
        )
      } else {
        imports.add(import(parentNode))
      }
    }
    return Pair(imports, extensionTypes)
  }

  /**
   * Parse an import instruction
   *
   * @param parentNode the optional parent node
   * @return an import node
   */
  private fun importExtensionType(): String {
    val classParts = mutableListOf(accept(IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      classParts.add(accept(IDENTIFIER).value)
    }
    return classParts.joinToString(".")
  }

  fun import(parentNode: CstNode? = null): ImportCstNode {
    val importToken = accept(TokenType.IMPORT)
    val staticImport = acceptOptional(TokenType.STATIC) != null
    val classParts = mutableListOf(accept(IDENTIFIER).value)
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
        return WildcardImportCstNode(parentNode, importToken, previous, classParts.joinToString(separator = "."))
      }
      classParts.add(accept(IDENTIFIER).value)
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
      StaticImportCstNode(parentNode, importToken, previous, className, method)
    } else {
      val asName = if (acceptOptional(TokenType.AS) != null) accept(IDENTIFIER).value else null
      SimpleImportCstNode(parentNode, importToken, previous, classParts.joinToString(separator = "."), asName)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return node
  }

  private fun parseOptPackage(): String? {
    if (acceptOptional(TokenType.PACKAGE) == null) return null
    val parts = mutableListOf<String>(accept(IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      parts.add(accept(IDENTIFIER).value)
    }
    acceptOptional(SEMI_COLON)
    return parts.joinToString(separator = ".")
  }

  fun dumbbells(): List<String> {
    val dumbbells = mutableListOf<String>()
    while (current.type == TokenType.DUMBBELL) {
      dumbbells.add(dumbbell())
    }
    return dumbbells
  }

  private fun dumbbell(): String {
    accept(TokenType.DUMBBELL)
    accept(OPEN_SIMPLE_QUOTE)
    val d = simpleStringPart()
    accept(TokenType.CLOSING_SIMPLE_QUOTE)
    return d
  }

  fun field(parentNode: ClassCstNode, annotations: List<AnnotationCstNode>, access: AccessCstNode): FieldCstNode {
    val tokenStart = current
    val type = parseType(parentNode)
    val name = accept(IDENTIFIER).value
    val initialValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
    acceptOptional(TokenType.SEMI_COLON)
    return FieldCstNode(parentNode, tokenStart, previous, access, annotations, type, name, initialValue)
  }

  private fun parseClass(
    sourceFile: SourceFileCstNode,
    packageName: String?, parentNode: CstNode?, annotations: List<AnnotationCstNode>,
    access: AccessCstNode,
    outerClassNode: ClassCstNode? = null
  ): ClassCstNode {
    val isExtensionClass = acceptOptional(TokenType.EXTENSION) != null
    val classToken = accept(TokenType.CLASS)
    val classSimpleName = accept(IDENTIFIER).value
    val className =
      if (outerClassNode != null) "${outerClassNode.className}\$$classSimpleName"
      else if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName

    // forExtensionClassType is actually optional, but this is not documented, because flemme
    val forExtensionClassType = if (isExtensionClass && current.type == FOR) {
      accept(FOR)
      parseType(parentNode)
    } else null

    val superType =
      if (acceptOptional(TokenType.EXTENDS) != null) parseType(parentNode)
      else null
    val interfaces = mutableListOf<TypeCstNode>()
    if (acceptOptional(TokenType.IMPLEMENTS) != null) {
      while (current.type == IDENTIFIER) {
        interfaces.add(parseType(parentNode))
        acceptOptional(TokenType.COMMA)
      }
    }
    val classNode = RegularClassCstNode(sourceFile, classToken, classToken, access, className, superType, interfaces, isExtensionClass, forExtensionClassType)
    classNode.annotations.addAll(annotations)

    accept(TokenType.BRACKETS_OPEN)
    while (current.type != TokenType.BRACKETS_CLOSE) {
      parseMember(sourceFile, packageName, classNode, classNode, classNode)
    }
    accept(TokenType.BRACKETS_CLOSE)
    return classNode
  }

  private fun parseEnum(
    sourceFile: SourceFileCstNode,
    packageName: String?, parentNode: CstNode?, annotations: List<AnnotationCstNode>,
    access: AccessCstNode,
    outerClassNode: ClassCstNode? = null
  ): EnumCstNode {
    val classToken = accept(TokenType.ENUM)
    val classSimpleName = accept(IDENTIFIER).value
    val className =
      if (outerClassNode != null) "${outerClassNode.className}\$$classSimpleName"
      else if (packageName != null) "$packageName.$classSimpleName"
      else classSimpleName

    val interfaces = mutableListOf<TypeCstNode>()
    if (acceptOptional(TokenType.IMPLEMENTS) != null) {
      while (current.type == IDENTIFIER) {
        interfaces.add(parseType(parentNode))
        acceptOptional(TokenType.COMMA)
      }
    }
    accept(TokenType.BRACKETS_OPEN)

    val names = mutableListOf<String>()
    while (current.type == IDENTIFIER) {
      names.add(accept(IDENTIFIER).value)
      acceptOptional(TokenType.COMMA)
    }

    accept(TokenType.BRACKETS_CLOSE)
    return EnumCstNode(sourceFile, classToken, previous, access, className, names)
  }

  fun method(parentNode: ClassCstNode, annotations: List<AnnotationCstNode>, access: AccessCstNode): AbstractMethodCstNode {
    val token = current
    val isAsync = acceptOptional(TokenType.ASYNC) != null
    val isConstructor = acceptOneOf(TokenType.FUN, TokenType.CONSTRUCTOR).type == TokenType.CONSTRUCTOR
    if (isAsync && isConstructor) throw MarcelParserException(current, "Constructors cannot be async")
    val node = if (!isConstructor) {
      val returnType = parseType(parentNode)
      val methodName = accept(IDENTIFIER).value
      MethodCstNode(parentNode, token, token, access, methodName, returnType, isAsync)
    } else ConstructorCstNode(parentNode, token, token, access)
    node.annotations.addAll(annotations)

    // parameters
    accept(TokenType.LPAR)
    val parameters = node.parameters
    var isVarArgs = false
    while (current.type != TokenType.RPAR) {
      val parameterTokenStart = current
      val parameterAnnotations = parseAnnotations(parentNode)
      val isThisParameter = acceptOptional(TokenType.THIS) != null && acceptOptional(TokenType.DOT) != null
      val type =
        if (!isThisParameter) parseType(parentNode)
        else TypeCstNode(parentNode, "", emptyList(), 0, previous, previous)
      if (isVarArgs) {
        // if it is true, and we went there, it means the three dots were not on the last parameter
        throw MarcelParserException(parameters.last().token, "Vararg parameter can only be the last parameter")
      } else {
        isVarArgs = !isThisParameter && acceptOptional(THREE_DOTS) != null
      }
      val parameterName = accept(IDENTIFIER).value

      val defaultValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(parentNode) else null
      parameters.add(MethodParameterCstNode(parentNode, parameterTokenStart, previous, parameterName, if (isVarArgs) type.withDimensions(type.arrayDimensions + 1) else type, defaultValue, parameterAnnotations, isThisParameter))
      acceptOptional(TokenType.COMMA)
    }
    node.isVarArgs = isVarArgs
    skip() // skip RPAR

    val statements = mutableListOf<StatementCstNode>()
    // super/this method call if any
    if (isConstructor && current.type == TokenType.COLON) {
      skip()
      when (val atom = atom(parentNode)) {
        is SuperConstructorCallCstNode, is ThisConstructorCallCstNode  -> statements.add(ExpressionStatementCstNode(atom))
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
      (node as? MethodCstNode)?.isSingleStatementFunction = true
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

  private fun parseAccess(parentNode: CstNode?): AccessCstNode {
    val startIndex = currentIndex
    val tokenStart = current
    val visibilityToken = acceptOptionalOneOf(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
      TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC
    val isStatic = acceptOptional(TokenType.STATIC) != null
    val isFinal = acceptOptional(TokenType.FINAL) != null
    val isInline = acceptOptional(TokenType.INLINE) != null
    val hasExplicitAccess = currentIndex > startIndex
    return AccessCstNode(parentNode, tokenStart, if (hasExplicitAccess) previous else current, isStatic, isInline, isFinal, visibilityToken, hasExplicitAccess)
  }

  internal fun parseType(parentNode: CstNode? = null): TypeCstNode {
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
    return TypeCstNode(parentNode, typeFragments.joinToString(separator = "$"), genericTypes, arrayDimension, tokenStart, previous)
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
    TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BYTE, TokenType.TYPE_SHORT, IDENTIFIER -> token.value
    TokenType.TYPE_BOOL -> "boolean"
    TokenType.DYNOBJ -> "DynamicObject"
    TokenType.END_OF_FILE -> throw eofException(token)
    else -> throw MarcelParserException(
      token,
      "Doesn't handle type ${token.type}"
    )
  }

  fun statement(parentNode: CstNode? = null): StatementCstNode {
    val token = next()
    try {
      return when (token.type) {
        TokenType.RETURN -> {
          val expr =
            if (current.type != TokenType.SEMI_COLON && current.type != TokenType.BRACKETS_CLOSE) expression(parentNode)
          else null
          ReturnCstNode(parentNode, expr, expr?.tokenStart ?: token, expr?.tokenEnd ?: token)
        }
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT,
        TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID,
        TokenType.TYPE_CHAR, TokenType.DYNOBJ -> {
          rollback()
          varDecl(parentNode)
        }
        IDENTIFIER -> {
          if ((current.type == IDENTIFIER && lookup(1)?.type == TokenType.ASSIGNMENT
                || current.type == TokenType.LT)
            // for object var decl without init expression
            || (current.type == IDENTIFIER && lookup(1)?.type == TokenType.SEMI_COLON)
            // for array var decl
            || (current.type == TokenType.DOT && lookup(1)?.type == TokenType.CLASS
                || current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE
                && lookup(2)?.type == IDENTIFIER))  {
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
          val rootIf = IfStatementCstNode(condition, statement(parentNode), null, parentNode, token, previous)
          var currentIf = rootIf
          while (current.type == TokenType.ELSE) {
            skip()
            if (acceptOptional(TokenType.IF) != null) {
              accept(TokenType.LPAR)
              val elseIfCondition = ifConditionExpression(parentNode)
              accept(TokenType.RPAR)
              val newIf = IfStatementCstNode(elseIfCondition, statement(parentNode), null, parentNode, token, previous)
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
        TokenType.DO -> {
          val statement = statement(parentNode)
          if (current.type != TokenType.WHILE) {
            // do statement just to have an inner scope
            if (statement is BlockCstNode) statement
            else BlockCstNode(listOf(statement), parentNode, token, statement.tokenEnd)
          } else {
            skip()
            accept(TokenType.LPAR)
            val condition = expression(parentNode)
            accept(TokenType.RPAR)
            DoWhileStatementCstNode(parentNode, token, condition.tokenEnd, statement, condition)
          }
        }
        TokenType.THROW -> {
          val expression = expression(parentNode)
          ThrowCstNode(parentNode, token, previous, expression)
        }
        TokenType.DEF -> {
          accept(TokenType.LPAR)
          val declarations = mutableListOf<Pair<TypeCstNode, String>?>()
          while (current.type != TokenType.RPAR) {
            if (current.type == IDENTIFIER && current.value.all { it == '_' }) {
              declarations.add(null)
              skip()
            } else {
              val varType = parseType(parentNode)
              val varName = accept(IDENTIFIER).value
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
          if (current.type == TokenType.LPAR) {
            // multi var declaration
            accept(TokenType.LPAR)
            val declarations = mutableListOf<Pair<TypeCstNode, String>>()
            while (current.type != TokenType.RPAR) {
              val varType = parseType(parentNode)
              val varName = accept(IDENTIFIER).value
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

            ForInMultiVarCstNode(declarations, expression, forBlock, parentNode, token, forBlock.tokenEnd)
          } else {
            val type = parseType(parentNode)
            if (lookup(1)?.type == TokenType.IN) {
              // for in statement
              val identifier = accept(IDENTIFIER).value
              accept(TokenType.IN)
              val expression = expression(parentNode)
              accept(TokenType.RPAR)
              val forBlock = statement(parentNode)
              ForInCstNode(type, identifier, expression, forBlock, parentNode, token, previous)
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
              ForVarCstNode(initStatement, condition, iteratorStatement, forBlock, parentNode, token, forBlock.tokenEnd)
            }
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
              if (e !is VariableDeclarationCstNode) throw MarcelParserException(
                e.token,
                "Can only declare variables in try with resources"
              )
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
            val exceptionVarName = accept(IDENTIFIER).value
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
      acceptOptional(SEMI_COLON)
    }
  }

  private fun ifConditionExpression(parentNode: CstNode?): ExpressionCstNode {
    return if (ParserUtils.isTypeToken(current.type) && lookup(1)?.type == IDENTIFIER) {
      val type = parseType(parentNode)
      val variableName = accept(IDENTIFIER).value
      accept(TokenType.ASSIGNMENT)
      val expression = expression(parentNode)
      TruthyVariableDeclarationCstNode(parentNode, type.tokenStart, expression.tokenEnd, type, variableName, expression)
    } else expression(parentNode)
  }

  private fun varDecl(parentNode: CstNode?) = varDecl(parentNode, parseType(parentNode))
  private fun varDecl(parentNode: CstNode?, type: TypeCstNode): VariableDeclarationCstNode {
    val identifierToken = accept(IDENTIFIER)
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

  fun expression(parentNode: CstNode? = null, maxPriority: Int = Int.MAX_VALUE): ExpressionCstNode {
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
        TokenType.QUESTION_MARK -> { // ternary
          val trueExpr = expression(parentNode)
          accept(TokenType.COLON)
          val falseExpr = expression(parentNode)
          TernaryCstNode(leftOperand, trueExpr, falseExpr, parentNode, leftOperand.tokenStart, falseExpr.tokenEnd)
        }
        TokenType.ELVIS -> {
          if (current.type == TokenType.THROW) {
            skip()
            val throwableExpression = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
            ElvisThrowCstNode(parentNode, leftOperand.tokenStart, throwableExpression.tokenEnd, leftOperand, throwableExpression)
          } else {
            val rightOperand = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
            BinaryOperatorCstNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
          }
        }
        else -> {
          val rightOperand = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
          BinaryOperatorCstNode(t.type, leftOperand, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        }
      }
      t = current
    }

    // method call without parenthesis
    if (a is ReferenceCstNode && current.type != SEMI_COLON && current.type != SEMI_COLON && !wasEndOfLine
      && FCALL_WITHOUT_PARENTHESIS_ALLOWED_TOKENS.contains(current.type)) {
      val (arguments, namedArguments) = parseFunctionArguments(parentNode = parentNode, withParenthesis = false)
      if (arguments.isNotEmpty() || namedArguments.isNotEmpty()) {
        a = FunctionCallCstNode(parentNode, a.value, castType = null, arguments, namedArguments, a.token, previous)
      }
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
        val optFlags = acceptOptional(IDENTIFIER)?.value
        if (optFlags != null) {
          for (char in optFlags) {
            RegexCstNode.FLAGS_MAP
            flags.add(RegexCstNode.FLAGS_MAP[char] ?: throw MarcelParserException(
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
        val referenceToken = accept(IDENTIFIER)
        val node = DirectFieldReferenceCstNode(parentNode, referenceToken.value, referenceToken)
        optIndexAccessCstNode(parentNode, node) ?: node
      }
      IDENTIFIER -> {
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
          FunctionCallCstNode(parentNode, token.value, null, listOf(parseLambda(token, parentNode)), emptyList(), token, previous)
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
        if (current.type == TokenType.FOR) {
          // array map filter
          skip()
          val varType = parseType(parentNode)
          val varName = accept(IDENTIFIER).value
          val inExpr = if (current.type == TokenType.IN) {
            skip()
            expression(parentNode)
          } else null
          val mapExpr: ExpressionCstNode?
          val filterExpr: ExpressionCstNode?
          if (current.type == TokenType.IF) { // find all, no map
            skip()
            filterExpr = expression(parentNode)
            mapExpr = null
          } else { // map, with optional filtering
            accept(TokenType.ARROW)
            mapExpr = expression(parentNode)
            filterExpr = if (current.type == TokenType.IF) {
              skip()
              expression(parentNode)
            } else null
          }
          accept(TokenType.SQUARE_BRACKETS_CLOSE)
          return MapFilterCstNode(parentNode, token, previous, varType, varName, inExpr, mapExpr, filterExpr)
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
          if (elements.size % 2 != 0) {
            throw MarcelParserException(token, "Invalid map: it should contains key/value mappings")
          }
          val entries = mutableListOf<Pair<ExpressionCstNode, ExpressionCstNode>>()
          for (i in elements.indices step 2) {
            entries.add(Pair(elements[i], elements[i + 1]))
          }
          MapCstNode(entries, parentNode, token, previous)
        } else ArrayCstNode(elements, parentNode, token, previous)
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
          if (token.type == TokenType.SUPER) SuperConstructorCallCstNode(parentNode, arguments, namedArguments, token, arguments.lastOrNull()?.tokenEnd ?: token)
          else ThisConstructorCallCstNode(parentNode, arguments, namedArguments, token, arguments.lastOrNull()?.tokenEnd ?: token)
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN || current.type == TokenType.QUESTION_SQUARE_BRACKETS_OPEN) {
          indexAccessCstNode(parentNode, ThisReferenceCstNode(parentNode, token))
        } else {
          if (token.type == TokenType.SUPER) SuperReferenceCstNode(parentNode, token)
          else ThisReferenceCstNode(parentNode, token)
        }
      }
      TokenType.NEW -> {
        val type = parseType(parentNode)
        accept(TokenType.LPAR)
        val arguments = parseFunctionArguments(parentNode)
        NewInstanceCstNode(parentNode, type, arguments.first, arguments.second, token, previous)
      }
      TokenType.MINUS -> unaryOperator(parentNode, token, ::UnaryMinusCstNode)
      TokenType.NOT -> unaryOperator(parentNode, token, ::NotCstNode)
      TokenType.VALUE_TRUE -> BoolCstNode(parentNode, true, token)
      TokenType.VALUE_FALSE -> BoolCstNode(parentNode, false, token)
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
        CharCstNode(parentNode, value[0], token, previous)
      }
      TokenType.ESCAPE_SEQUENCE -> StringCstNode(parentNode, escapedSequenceValue(token.value), token, previous)
      TokenType.INCR -> {
        val identifierToken = accept(IDENTIFIER)
        IncrCstNode(parentNode, identifierToken.value, 1, false, token, identifierToken)
      }
      TokenType.DECR -> {
        val identifierToken = accept(IDENTIFIER)
        IncrCstNode(parentNode, identifierToken.value, -1, false, token, identifierToken)
      }
      TokenType.LPAR -> {
        val node = WrappedExpressionCstNode(expression(parentNode))
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
      TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID, TokenType.TYPE_CHAR, TokenType.DYNOBJ -> {
        val arrayDimensions = parseArrayDimensions()
        accept(TokenType.DOT)
        accept(TokenType.CLASS)
        ClassReferenceCstNode(parentNode,
          TypeCstNode(parentNode,
            if (token.type == DYNOBJ) DynamicObject::class.java.name else token.value, emptyList(), arrayDimensions, token, previous), token, previous)
      }
      TokenType.WHEN, TokenType.SWITCH -> {
        if (token.type == TokenType.WHEN && current.type != TokenType.BRACKETS_OPEN) {
          return whenIn(parentNode, token, negate = false, allowFind = true)
        }
        var switchExpression: ExpressionCstNode? = null
        var varDecl: VariableDeclarationCstNode? = null
        if (token.type == TokenType.SWITCH) {
          accept(TokenType.LPAR)
          if (ParserUtils.isTypeToken(current.type) && lookup(1)?.type == IDENTIFIER && lookup(2)?.type == TokenType.ASSIGNMENT) {
            val type = parseType(parentNode)
            val varName = accept(IDENTIFIER).value
            varDecl = VariableDeclarationCstNode(type, varName, null, parentNode, type.tokenStart, current)
            accept(TokenType.ASSIGNMENT)
          }
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
        if (switchExpression == null) WhenCstNode(parentNode, token, previous, branches, elseStatement)
        else SwitchCstNode(parentNode, token, previous, branches, elseStatement, varDecl, switchExpression)
      }
      TokenType.NOT_WHEN -> whenIn(parentNode, token, negate = true)
      TokenType.BRACKETS_OPEN -> parseLambda(token, parentNode)
      TokenType.ASYNC ->  block(parentNode).let {
        AsyncBlockCstNode(parentNode, token, it.tokenEnd, it)
      }
      else -> throw MarcelParserException(token, "Not supported $token", eof)
    }
  }

  private fun whenIn(parentNode: CstNode?, token: LexToken, negate: Boolean, allowFind: Boolean = false): ExpressionCstNode {
    val type = parseType(parentNode)
    val varName = accept(IDENTIFIER).value
    val inExpr = if (current.type == TokenType.IN) {
      skip()
      expression(parentNode)
    } else null
    val operatorToken = next()
    val filterExpr = expression(parentNode)
    if (allowFind && operatorToken.type == TokenType.ARROW) {
      return FindInCstNode(parentNode, token, previous, type, varName, inExpr, filterExpr)
    }
    return when (operatorToken.type) {
      TokenType.OR_ARROW -> AnyInCstNode(parentNode, token, previous, type, varName, inExpr, filterExpr, negate)
      TokenType.AND_ARROW -> AllInCstNode(parentNode, token, previous, type, varName, inExpr, filterExpr, negate)
      else -> throw MarcelParserException(token, "Invalid token ${token.type}")
    }
  }
  private inline fun unaryOperator(parentNode: CstNode?, token: LexToken,
                            nodeCreator: (ExpressionCstNode, CstNode?, LexToken, LexToken) -> ExpressionCstNode): ExpressionCstNode {
    val rootExpr = expression(parentNode, UNARY_PRIORITY)
    return if (rootExpr !is BinaryOperatorCstNode || ParserUtils.getPriority(rootExpr.tokenType) <= UNARY_PRIORITY) nodeCreator(rootExpr, parentNode, token, previous)
    else {
      var expr = rootExpr
      while (expr is BinaryOperatorCstNode
        && expr.leftOperand is BinaryOperatorCstNode
        && ParserUtils.getPriority((expr.leftOperand as BinaryOperatorCstNode).tokenType) > UNARY_PRIORITY) {
        expr = expr.leftOperand
      }
      expr as BinaryOperatorCstNode
      expr.leftOperand = nodeCreator(expr.leftOperand, parentNode, token, previous)
      rootExpr
    }
  }

  private fun stringPart(parentNode: CstNode?): ExpressionCstNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringCstNode(parentNode, token.value, token, token)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> {
        val identifierToken = accept(IDENTIFIER)
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

  // BRACKETS_OPEN should have alreadt been parsed
  private fun parseLambda(token: LexToken, parentNode: CstNode?): LambdaCstNode {
    val parameters = mutableListOf<LambdaCstNode.MethodParameterCstNode>()
    var explicit0Parameters = false
    // first parameter with no type specified
    if (current.type == IDENTIFIER && lookup(1)?.type in listOf(TokenType.COMMA, TokenType.ARROW, TokenType.LT) // LT for generic types
      || ParserUtils.isTypeToken(current.type) && lookup(1)?.type == IDENTIFIER && lookup(2)?.type in listOf(TokenType.COMMA, TokenType.ARROW)) {
      while (current.type != TokenType.ARROW) {
        val firstToken = current
        val parameter = if (lookup(1)?.type == IDENTIFIER || lookup(1)?.type == TokenType.LT) {
          val type = parseType(parentNode)
          val identifier = accept(IDENTIFIER)
          LambdaCstNode.MethodParameterCstNode(parentNode, firstToken, identifier, type, identifier.value)
        } else {
          next()
          LambdaCstNode.MethodParameterCstNode(parentNode, firstToken, firstToken, null, firstToken.value)
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
    return LambdaCstNode(parentNode, token, previous, parameters, block, explicit0Parameters)
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

  /**
   * Parse function arguments. LPAR token must have already been accepted.
   *
   * @param parentNode the parent node
   * @param allowLambdaLastArg whether to allow the lambda last argument syntax or not
   * @param withParenthesis whether the function call was made using parenthesis or not
   * @return a Pair of the list of positional arguments, and the list of named arguments
   */
  private fun parseFunctionArguments(parentNode: CstNode? = null, allowLambdaLastArg: Boolean = true, withParenthesis: Boolean = true): Pair<MutableList<ExpressionCstNode>, MutableList<Pair<String, ExpressionCstNode>>> {
    val arguments = mutableListOf<ExpressionCstNode>()
    val namedArguments = mutableListOf<Pair<String, ExpressionCstNode>>()
    while ((withParenthesis && current.type != RPAR)
      //
      || (!withParenthesis && !wasEndOfLine && !eof && current.type != END_OF_FILE && current.type != BRACKETS_OPEN && current.type != RPAR)) {
      if (current.type == IDENTIFIER && lookup(1)?.type == TokenType.COLON) {
        val identifierToken = accept(IDENTIFIER)
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
    if (withParenthesis) accept(RPAR) // skipping RPAR
    if (allowLambdaLastArg && current.type == BRACKETS_OPEN) {
      // fcall ending with a lambda
      arguments.add(parseLambda(next(), null))
    }
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
      if (current.type == IDENTIFIER && lookup(1)?.type == TokenType.ASSIGNMENT) {
        while (current.type != TokenType.RPAR) {
          val attributeName = accept(IDENTIFIER).value
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
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0L
        }
        LongCstNode(parentNode, value, token)
      } else {
        val value = try {
          numberString.toInt(radix)
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
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
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0.0
        }
        DoubleCstNode(parentNode, value, token)
      } else {
        val value = try {
          valueString.toFloat()
        } catch (e: NumberFormatException) {
          recordError(MarcelParserException.malformedNumber(e, token, eof))
          0f
        }
        FloatCstNode(parentNode, value, token)
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
      throw MarcelParserException(current, "Expected $type but got ${token.type}")
    }
    forward()
    return token
  }

  private fun forward() {
    currentIndex++
    while (currentIndex < tokens.size && tokens[currentIndex].type == LINE_RETURN) currentIndex++
  }

  private fun rollback() {
    currentIndex--
    while (currentIndex >= 0 && tokens[currentIndex].type == LINE_RETURN) currentIndex--
  }

  private fun lookup(howFar: Int): LexToken? {
    var index = currentIndex
    repeat(howFar) {
      index++
      while (index < tokens.size && tokens[index].type == LINE_RETURN) index++
    }
    return tokens.getOrNull(index)
  }

  private fun acceptOneOf(vararg types: TokenType): LexToken {
    val token = current
    if (token.type !in types) {
      throw MarcelParserException(
        current,
        "Expected token of type ${types.contentToString()} but got ${token.type}"
      )
    }
    forward()
    return token
  }

  private fun acceptOptionalOneOf(vararg types: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type in types) {
      forward()
      return token
    }
    return null
  }

  private fun acceptOptional(t: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type == t) {
      forward()
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

  private fun skip() = forward()

  private fun skip(howMuch: Int) = repeat(howMuch) {
    skip()
  }


  private fun next(): LexToken {
    checkEof()
    return tokens[currentIndex].apply {
      forward()
    }
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