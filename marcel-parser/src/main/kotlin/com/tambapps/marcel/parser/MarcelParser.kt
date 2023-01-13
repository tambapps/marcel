package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.*

import com.tambapps.marcel.parser.ast.TokenNodeType.*
import org.objectweb.asm.Opcodes
import java.util.concurrent.ThreadLocalRandom

class MarcelParser(private val className: String, private val tokens: List<LexToken>) {

  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + ThreadLocalRandom.current().nextInt(), tokens)

  private var currentIndex = 0

  private val current: LexToken
    get() {
      checkEof()
      return tokens[currentIndex]
    }

  fun parse(): ModuleNode {
    return ModuleNode(mutableListOf(ClassNode(
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, className, mutableListOf(
      MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
        mutableListOf(
          statement()
    ))))))
  }

  private fun statement(): TokenNode {
    val token = next()
    when (token.type) {
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.LPAR) {
          skip()
          val fCall = TokenNode(FUNCTION_CALL, token.value)
          while (current.type != TokenType.RPAR) {
            fCall.addChild(expression())
            if (current.type == TokenType.RPAR) {
              break
            } else {
              accept(TokenType.COMMA)
            }
          }
          skip() // skipping PARENT_CLOSE
          return fCall
        } else {
          throw UnsupportedOperationException("Not supported yet")
        }
      }
      else -> {
        throw MarcelParsingException("Unexpected token of type ${token.type}")
      }
    }
  }

  private fun expression(): TokenNode {
    val expr = expression(Int.MAX_VALUE)
    if (current.type == TokenType.QUESTION_MARK) {
      skip()
      val trueExpr = expression()
      accept(TokenType.COLON)
      val falseExpr = expression()
      return TokenNode(TERNARY, mutableListOf(expr, trueExpr, falseExpr))
    }
    return expr
  }

  private fun expression(maxPriority: Int): TokenNode {
    var a = atom()
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      /* TODO
      moveForward()
      TokenNode N = new TokenNode(T, BINARY_OPERATOR_MAP.get(T.type))
      N.addChild(A)
      N.addChild(expression(PRIORITY_MAP.get(T.type) + ASSOCIATIVITY_MAP.get(T.type)))
      A = N
      T = getCurrent()
      */
    }
    return a
  }

  private fun atom(): TokenNode {
    val token = next()
    return when (token.type) {
      TokenType.INTEGER -> TokenNode(INTEGER, token.value)
      else -> {
        throw UnsupportedOperationException("Not supported yet")
      }
    }
  }
    /*

    private TokenNode expression() {
    TokenNode expr = expression(Integer.MAX_VALUE)
    if (getCurrent().type == TokenType.QUESTION_MARK) {
      //terNode children: 1)evaluation 2)true value 3)false value
      TokenNode terNode = new TokenNode(accept(TokenType.QUESTION_MARK))
      terNode.addChildren(expr, expression())
      accept(TokenType.COLON)
      terNode.addChild(expression())
      return terNode
    }
    return expr
  }

  private TokenNode expression(int maxP) {
    TokenNode A = atom()
    Token T = getCurrent()
    while (T.type.isBinaryOperator() && PRIORITY_MAP.get(T.type) < maxP) {
      moveForward()
      TokenNode N = new TokenNode(T, BINARY_OPERATOR_MAP.get(T.type))
      N.addChild(A)
      N.addChild(expression(PRIORITY_MAP.get(T.type) + ASSOCIATIVITY_MAP.get(T.type)))
      A = N
      T = getCurrent()
    }
    return A
  }
   */
  private fun accept(t: TokenType): LexToken {
    val token = current
    if (token.type != t) {
      throw MarcelParsingException("Expected token of type $t but got ${token.type}")
    }
    currentIndex++
    return token
  }

  private fun accept(t: TokenType, required: Boolean): LexToken {
    val token = current
    if (token.type == t) {
      currentIndex++
    } else if (required) {
      throw MarcelParsingException("Expected token of type $t but got ${token.type}")
    }
    return token
  }

  private fun skip() {
    currentIndex++
  }
  private fun next(): LexToken {
    checkEof()
    return tokens[currentIndex++]
  }

  fun reset() {
    currentIndex = 0
  }

  private fun checkEof() {
    if (currentIndex >= tokens.size) {
      throw MarcelParsingException("Unexpected end of file")
    }
  }
}