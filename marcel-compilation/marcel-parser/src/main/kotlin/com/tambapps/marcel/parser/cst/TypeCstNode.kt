package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

/**
 * Type cst node
 *
 * @property value the type string
 * @property genericTypes the generic types
 * @property arrayDimensions the number of array dimensions
 *
 * @param parent the parent node if any
 * @param tokenStart the token start
 * @param tokenEnd the token end
 */
class TypeCstNode constructor(
  parent: CstNode?,
  override val value: String, // the type
  val genericTypes: List<TypeCstNode>,
  val arrayDimensions: Int,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    fun withDimensions(arrayDimensions: Int) = TypeCstNode(parent, value, genericTypes, arrayDimensions, tokenStart, tokenEnd)

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
        if (other !is TypeCstNode) return false

        if (arrayDimensions != other.arrayDimensions) return false
        if (value != other.value) return false
        if (genericTypes != other.genericTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = arrayDimensions
        result = 31 * result + value.hashCode()
        result = 31 * result + genericTypes.hashCode()
        return result
    }

    fun toString(nullable: Boolean) = if (nullable) "${toString()}?" else toString()

}