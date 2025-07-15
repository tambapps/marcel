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
) : AbstractCstNode(parent, tokenStart, tokenEnd), IdentifiableCstNode {

    fun withDimensions(arrayDimensions: Int) = TypeCstNode(parent, value, genericTypes, arrayDimensions, tokenStart, tokenEnd)

    override fun toString() = buildString {
      append(value)
      if (genericTypes.isNotEmpty()) genericTypes.joinTo(separator = ", ", prefix = "<", postfix = ">", buffer = this)
      repeat(arrayDimensions) {
        append("[]")
      }
    }

  fun toString(nullable: Boolean) = if (nullable) "${toString()}?" else toString()

  override fun isEqualTo(other: CstNode): Boolean {
    if (other !is TypeCstNode) return false

    if (arrayDimensions != other.arrayDimensions) return false
    if (value != other.value) return false
    if (genericTypes notEq other.genericTypes) return false
    return true
  }
}