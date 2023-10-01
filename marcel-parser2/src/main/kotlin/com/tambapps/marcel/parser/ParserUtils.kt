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

    Pair(MUL, 2),
    Pair(DIV, 2),
    Pair(MODULO, 2),
    Pair(FIND, 2),

    Pair(PLUS, 3),
    Pair(MINUS, 3),

    Pair(LEFT_SHIFT, 4),
    Pair(RIGHT_SHIFT, 4),

    Pair(GT, 5),
    Pair(LT, 5),
    Pair(GOE, 5),
    Pair(LOE, 5),
    // instanceof 5

    Pair(EQUAL, 6),
    Pair(NOT_EQUAL, 6),
    Pair(IS, 6),
    Pair(IS_NOT, 6),

    Pair(AND, 7),
    Pair(OR, 8),

    Pair(ASSIGNMENT, 9),
    Pair(MINUS_ASSIGNMENT, 9),
    Pair(PLUS_ASSIGNMENT, 9),
    Pair(MUL_ASSIGNMENT, 9),
    Pair(DIV_ASSIGNMENT, 9),
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