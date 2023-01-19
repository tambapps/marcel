package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParsingException
import org.objectweb.asm.Opcodes

enum class ComparisonOperator(val tokenType: TokenType, val iOpCode: Int, val symbolString: String) {
  EQUAL(TokenType.EQUAL, Opcodes.IF_ICMPEQ, "=="),
  NOT_EQUAL(TokenType.NOT_EQUAL, Opcodes.IF_ICMPNE, "!="),
  LT(TokenType.LT,Opcodes.IF_ICMPLT, "<"),
  GT(TokenType.GT,Opcodes.IF_ICMPGT, ">"),
  LOE(TokenType.LOE,Opcodes.IF_ICMPLE, "<="),
  GOE(TokenType.GOE,Opcodes.IF_ICMPGE, ">=");

  companion object {

    fun fromTokenType(tokenType: TokenType): ComparisonOperator {
      return values().find { it.tokenType == tokenType }
        ?: throw MarcelParsingException("Invalid comparison operator token $tokenType")
    }
  }

  override fun toString(): String {
    return symbolString
  }
}