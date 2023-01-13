package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.lexer.TokenType.*

object ParserUtils {

  private val BINARY_OPERATOR = setOf(
    POWER, MUL, MODULO, PLUS, MINUS, AND, EQUAL, NOT_EQUAL, GT, LT, GOE, LOE, OR, ASSIGNEMENT
  )
  const val LEFT = 0
  const val RIGHT = 1
  private val PRIORITY_MAP = mapOf(
    Pair(POWER, 1),
    Pair(NOT, 1),
    Pair(MUL, 2),
    Pair(MODULO, 2),
    Pair(PLUS, 2),
    Pair(MINUS, 2),
    Pair(AND, 2),
    Pair(EQUAL, 2),
    Pair(NOT_EQUAL, 2),
    Pair(GT, 2),
    Pair(LT, 2),
    Pair(GOE, 2),
    Pair(LOE, 2),
    Pair(OR, 2),
    Pair(ASSIGNEMENT, 2)
  )

  private val RIGHT_ASSOCIATIVITY_OPERATOR = listOf(POWER, ASSIGNEMENT)

  fun isBinaryOperator(t: TokenType): Boolean {
    return t in BINARY_OPERATOR
  }

  fun getAssociativity(t: TokenType): Int {
    return if (t in RIGHT_ASSOCIATIVITY_OPERATOR) RIGHT else LEFT
  }

  fun getPriority(t: TokenType): Int {
    return PRIORITY_MAP[t]!!
  }
}