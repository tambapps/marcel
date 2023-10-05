package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.*
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
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
    sourceFile.instructions.add(statement(sourceFile))

    if (errors.isNotEmpty()) {
      throw MarcelParser2Exception(errors)
    }
    return sourceFile
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
    val genericTypes = mutableListOf<String>()
    if (current.type == TokenType.LT) {
      skip()
      genericTypes.add(parseTypeFragment(next()))
      while (current.type == TokenType.COMMA) {
        skip()
        genericTypes.add(parseTypeFragment(next()))
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
    val expr = expression(parentNode)
    return ExpressionStatementCstNode(parentNode, expr, expr.tokenStart, acceptOptional(TokenType.SEMI_COLON) ?: expr.tokenEnd)
  }

  fun expression(parentNode: CstNode? = null): CstExpressionNode {
    // TODO
    return atom(parentNode)
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
      else -> TODO()
    }
  }

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