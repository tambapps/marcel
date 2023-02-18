package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

enum class ComparisonOperator(val tokenType: TokenType, val iOpCode: Int,
                              val objectOpCode: Int,
                              val symbolString: String) {
  EQUAL(TokenType.EQUAL, Opcodes.IF_ICMPEQ, Opcodes.IF_ACMPEQ, "=="),
  NOT_EQUAL(TokenType.NOT_EQUAL, Opcodes.IF_ICMPNE,  Opcodes.IF_ACMPNE, "!="),
  LT(TokenType.LT,Opcodes.IF_ICMPLT, -1, "<"),
  GT(TokenType.GT,Opcodes.IF_ICMPGT, -1, ">"),
  LOE(TokenType.LOE,Opcodes.IF_ICMPLE, -1, "<="),
  GOE(TokenType.GOE,Opcodes.IF_ICMPGE, -1, ">=");

  companion object {

    val INT_LIKE_COMPARABLE_TYPES = listOf(JavaType.int, JavaType.short, JavaType.byte, JavaType.char)

    fun fromTokenType(tokenType: TokenType): ComparisonOperator {
      return values().find { it.tokenType == tokenType }
        ?: throw MarcelParsingException("Invalid comparison operator token $tokenType")
    }
  }

  override fun toString(): String {
    return symbolString
  }
}