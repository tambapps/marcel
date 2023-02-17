package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.lexer.TokenType.*
import org.objectweb.asm.Opcodes

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
    Pair(PLUS, 3),
    Pair(MINUS, 3),
    Pair(AND, 4),
    Pair(EQUAL, 5),
    Pair(NOT_EQUAL, 5),
    Pair(GT, 5),
    Pair(LT, 5),
    Pair(GOE, 5),
    Pair(LOE, 5),
    Pair(OR, 5),
    Pair(LEFT_SHIFT, 5),
    Pair(RIGHT_SHIFT, 5),
    Pair(ASSIGNMENT, 6),
    Pair(MINUS_ASSIGNMENT, 6),
    Pair(PLUS_ASSIGNMENT, 6),
    Pair(MUL_ASSIGNMENT, 6),
    Pair(DIV_ASSIGNMENT, 6),
    Pair(IS, 7),
    Pair(IS_NOT, 7),
  )

  private val RIGHT_ASSOCIATIVITY_OPERATOR = listOf(POWER, ASSIGNMENT)

  val TOKEN_VISIBILITY_MAP = mapOf(
    Pair(VISIBILITY_PUBLIC, Opcodes.ACC_PUBLIC),
    Pair(VISIBILITY_PROTECTED, Opcodes.ACC_PROTECTED),
    Pair(VISIBILITY_INTERNAL, 0),
    Pair(VISIBILITY_PRIVATE, Opcodes.ACC_PRIVATE)
  )
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
      TYPE_LONG, TYPE_INT, TYPE_SHORT, TYPE_VOID, IDENTIFIER)
  }
}