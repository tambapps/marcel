package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.TokenType

object ParserUtils {
  private val BINARY_OPERATOR = setOf<TokenType>()
  private val PRIORITY_MAP = mapOf<TokenType, Int>()

  fun isBinaryOperator(t: TokenType): Boolean {
    return t in BINARY_OPERATOR
  }

  fun getPriority(t: TokenType): Int {
    return PRIORITY_MAP[t]!!
  }
}