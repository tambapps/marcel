package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.lexer.TokenType.*

object ParserUtils {

  const val LEFT = 0
  const val RIGHT = 1
  private val PRIORITY_MAP = mapOf(
    Pair(POWER, 1),
    Pair(DOT, 1),
    Pair(QUESTION_DOT, 1),
    Pair(NOT, 1),

    Pair(TWO_DOTS, 2),
    Pair(TWO_DOTS_END_EXCLUSIVE, 2),

    Pair(MUL, 3),
    Pair(DIV, 3),
    Pair(MODULO, 3),
    Pair(FIND, 3),

    Pair(PLUS, 4),
    Pair(MINUS, 4),

    Pair(LEFT_SHIFT, 5),
    Pair(RIGHT_SHIFT, 5),

    Pair(GT, 6),
    Pair(LT, 6),
    Pair(GOE, 6),
    Pair(LOE, 6),
    Pair(INSTANCEOF, 6),
    Pair(NOT_INSTANCEOF, 6),
    Pair(AS, 6),

    Pair(EQUAL, 7),
    Pair(NOT_EQUAL, 7),
    Pair(IS, 7),
    Pair(IS_NOT, 7),

    Pair(AND, 8),
    Pair(OR, 9),

    Pair(ELVIS, 10),

    Pair(ASSIGNMENT, 11),
    Pair(MINUS_ASSIGNMENT, 11),
    Pair(PLUS_ASSIGNMENT, 11),
    Pair(MUL_ASSIGNMENT, 11),
    Pair(DIV_ASSIGNMENT, 11),
  )

  private val RIGHT_ASSOCIATIVITY_OPERATOR = listOf(POWER, ASSIGNMENT)

  fun isBinaryOperator(t: TokenType): Boolean {
    return t in PRIORITY_MAP.keys
  }

  fun getAssociativity(t: TokenType): Int {
    return if (t in RIGHT_ASSOCIATIVITY_OPERATOR) RIGHT else LEFT
  }

  fun getPriority(t: TokenType): Int {
    return PRIORITY_MAP[t]!!
  }

  fun isTypeToken(tokenType: TokenType): Boolean {
    return tokenType in listOf(TYPE_BOOL, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BYTE, TYPE_CHAR,
      TYPE_LONG, TYPE_INT, TYPE_SHORT, TYPE_VOID, IDENTIFIER, DYNOBJ)
  }
}