package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class MethodNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: AccessNode,
  val name: String,
  val returnTypeNode: TypeNode,
) :
  AbstractMethodNode(parent, tokenStart, tokenEnd, accessNode) {
    var isSingleStatementFunction = false // whether if is fun type method() -> statement()
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MethodNode) return false
    if (!super.equals(other)) return false

    if (name != other.name) return false
    if (returnTypeNode != other.returnTypeNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + returnTypeNode.hashCode()
    return result
  }

  override fun toString() = StringBuilder().apply {
    append("fun ")
    append(returnTypeNode)
    append(" ")
    append(name)
    append(parameters.joinToString(separator = ", ", prefix = "(", postfix = ")"))
  }.toString()
}