package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AbstractMethodNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.CstAccessNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringNode
import com.tambapps.marcel.parser.cst.expression.VariableAssignmentCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.*
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
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
    val sourceFile = SourceFileCstNode(fileName = classSimpleName, tokenStart = tokens.first(), tokenEnd = tokens.last())
    while (current.type != TokenType.END_OF_FILE) {
      if (current.type == TokenType.CLASS
        // visibility
        || lookup(1)?.type == TokenType.CLASS
        // visibility|extension + abstract
        || lookup(2)?.type == TokenType.CLASS) {
        TODO()
      } else if (current.type == TokenType.FUN
        // visibility
        || lookup(1)?.type == TokenType.FUN
        // visibility|extension + abstract
        || lookup(2)?.type == TokenType.FUN) {
        when (val method = method(sourceFile)) {
          is MethodCstNode -> sourceFile.methods.add(method)
          is ConstructorCstNode -> sourceFile.constructors.add(method)
        }
      } else {
        sourceFile.statements.add(statement(sourceFile))
      }
    }

    if (errors.isNotEmpty()) {
      throw MarcelParser2Exception(errors)
    }
    return sourceFile
  }

  fun method(parentNode: CstNode?): AbstractMethodNode {
    val token = current
    val isConstructor = accept(TokenType.FUN, TokenType.CONSTRUCTOR).type == TokenType.CONSTRUCTOR
    val accessNode = parseAccess(parentNode)
    val node = if (!isConstructor) {
      val returnType = parseType(parentNode)
      val methodName = accept(TokenType.IDENTIFIER).value
      MethodCstNode(parentNode, token, token, accessNode, methodName, returnType)
    } else ConstructorCstNode(parentNode, token, token, accessNode)

    // parameters
    accept(TokenType.LPAR)
    val parameters = mutableListOf<MethodParameterCstNode>()
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

    if (isConstructor && current.type != TokenType.RPAR) {
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
    val tokenStart = current
    val visibilityToken = acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
      TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC
    val isStatic = acceptOptional(TokenType.STATIC) != null
    val isFinal = acceptOptional(TokenType.FINAL) != null
    val isInline = acceptOptional(TokenType.INLINE) != null
    return CstAccessNode(parentNode, tokenStart, previous, isStatic, isInline, isFinal, visibilityToken)
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
    var arrayDimension = 0
    while (current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup(1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
      skip(2)
      arrayDimension++
    }
    return TypeCstNode(parentNode, typeFragments.joinToString(separator = "."), genericTypes, arrayDimension, tokenStart, previous)
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
          ReturnCstNode(parentNode, expr, expr.tokenStart, acceptOptional(TokenType.SEMI_COLON) ?: expr.tokenEnd)
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
            ExpressionStatementCstNode(parentNode, expr, expr.tokenStart, acceptOptional(TokenType.SEMI_COLON) ?: expr.tokenEnd)
          }
        }
        else -> {
          rollback()
          val expr = expression(parentNode)
          ExpressionStatementCstNode(parentNode, expr, expr.tokenStart, acceptOptional(TokenType.SEMI_COLON) ?: expr.tokenEnd)
        }
      }
    } finally {
      acceptOptional(TokenType.SEMI_COLON)
    }
 }

  private fun varDecl(parentNode: CstNode?): VariableDeclarationCstNode {
    val type = parseType(parentNode)
    val identifierToken = accept(TokenType.IDENTIFIER)
    accept(TokenType.ASSIGNMENT)
    val expression = if (current.type != TokenType.SEMI_COLON) expression(parentNode) else null
    return VariableDeclarationCstNode(type, identifierToken.value, expression, parentNode, type.tokenStart, expression?.tokenEnd ?: identifierToken)
  }

  fun expression(parentNode: CstNode? = null): CstExpressionNode {
    // TODO
    return expression(parentNode, Int.MAX_VALUE)
  }

  private fun expression(parentNode: CstNode?, maxPriority: Int): CstExpressionNode {
    var a = atom(parentNode)
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      next()
      val leftOperand = a
      val rightOperand = expression(parentNode, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
      a = operator(parentNode, t, leftOperand, rightOperand)
      t = current
    }
    return a
  }

  private fun operator(parentNode: CstNode?, token: LexToken, leftOperand: CstExpressionNode, rightOperand: CstExpressionNode): CstExpressionNode {
    return when(token.type) {
      TokenType.ASSIGNMENT -> when (leftOperand) {
        is ReferenceCstNode -> VariableAssignmentCstNode(leftOperand.value, rightOperand, parentNode, leftOperand.tokenStart, rightOperand.tokenEnd)
        else -> TODO()
      }
      else -> TODO()
    }
  }

  fun atom(parentNode: CstNode? = null): CstExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.INTEGER, TokenType.FLOAT -> {
        return if (current.type == TokenType.LT || current.type == TokenType.GT || current.type == TokenType.TWO_DOTS) {
          TODO()
        } else {
          parseNumberConstant(parentNode=parentNode, token=token)
        }
      }
      TokenType.OPEN_QUOTE -> { // double quotes
        val parts = mutableListOf<CstExpressionNode>()
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
        StringCstNode(parentNode, builder.toString(), token, previous)
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
        } else if (current.type == TokenType.LT  && lookup(1)?.type == TokenType.TWO_DOTS
          || current.type == TokenType.GT && lookup(1)?.type == TokenType.TWO_DOTS || current.type == TokenType.TWO_DOTS) {
          TODO("Range node. Might convert it into an operator though")
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN
          || (current.type == TokenType.QUESTION_MARK && lookup(1)?.type == TokenType.SQUARE_BRACKETS_OPEN)) {
          indexAccessCstNode(parentNode, ReferenceCstNode(parentNode, token.value, token))
        } else if (current.type == TokenType.DOT && lookup(1)?.type == TokenType.CLASS) {
          skip(2) // skip dot and class
          ClassReferenceCstNode(parentNode, token.value, token, previous)
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
        val elements = mutableListOf<CstExpressionNode>()
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
          val entries = mutableListOf<Pair<CstExpressionNode, CstExpressionNode>>()
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
      else -> TODO(token.type.name)
    }
  }

  private fun stringPart(parentNode: CstNode?): CstExpressionNode {
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
  private fun parseFunctionArguments(parentNode: CstNode? = null): Pair<MutableList<CstExpressionNode>, MutableList<Pair<String, CstExpressionNode>>> {
    val arguments = mutableListOf<CstExpressionNode>()
    val namedArguments = mutableListOf<Pair<String, CstExpressionNode>>()
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


  private fun indexAccessCstNode(parentNode: CstNode?, ownerNode: CstNode): IndexAccessCstNode {
    val isSafeIndex = acceptOptional(TokenType.QUESTION_MARK) != null
    val tokenStart = current
    skip()
    val indexArguments = mutableListOf<CstNode>()
    while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
      indexArguments.add(expression(parentNode))
      if (current.type == TokenType.COMMA) skip()
    }
    skip() // skip brackets close
    return IndexAccessCstNode(parentNode, ownerNode, indexArguments, isSafeIndex, tokenStart, previous)
  }

  private fun optIndexAccessCstNode(parentNode: CstNode?, ownerNode: CstNode): IndexAccessCstNode? {
    if (current.type == TokenType.SQUARE_BRACKETS_OPEN
      || (current.type == TokenType.QUESTION_MARK && lookup(1)?.type == TokenType.SQUARE_BRACKETS_OPEN)) {
      indexAccessCstNode(parentNode, ownerNode)
    }
    return null
  }

  private fun parseAnnotations(parentNode: CstNode?): List<AnnotationCstNode> {
    val classAnnotations = mutableListOf<AnnotationCstNode>()
    while (current.type == TokenType.AT) {
      classAnnotations.add(parseAnnotation(parentNode))
    }
    return classAnnotations
  }

  private fun parseAnnotation(parentNode: CstNode?): AnnotationCstNode {
    val token = next()
    val type = parseType(parentNode)
    val attributes = mutableListOf<Pair<String, CstExpressionNode>>()
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

  private fun parseNumberConstant(parentNode: CstNode? = null, token: LexToken): CstExpressionNode {
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