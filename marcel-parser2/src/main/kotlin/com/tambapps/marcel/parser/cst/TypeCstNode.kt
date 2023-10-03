package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class TypeCstNode(
  parent: CstNode?,
  override val value: String, // the type
  val genericTypes: List<String>,
  val arrayDimensions: Int,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString(): String {
        val builder = StringBuilder()
            .append(value)
        if (genericTypes.isNotEmpty()) builder.append(genericTypes.joinTo(separator = ", ", prefix = "<", postfix = ">", buffer = builder))
        for (i in 1..arrayDimensions) {
            builder.append("[]")
        }
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TypeCstNode

        if (value != other.value) return false
        if (genericTypes != other.genericTypes) return false
        if (arrayDimensions != other.arrayDimensions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + genericTypes.hashCode()
        result = 31 * result + arrayDimensions
        return result
    }
}