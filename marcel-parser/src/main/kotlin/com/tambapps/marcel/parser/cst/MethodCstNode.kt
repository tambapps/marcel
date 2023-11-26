package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class MethodCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: CstAccessNode,
  val name: String,
  val returnTypeCstNode: TypeCstNode,
) :
  AbstractMethodCstNode(parent, tokenStart, tokenEnd, accessNode) {
    var isSingleStatementFunction = false // whether if is fun type method() -> statement()
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MethodCstNode) return false
    if (!super.equals(other)) return false

    if (name != other.name) return false
    if (returnTypeCstNode != other.returnTypeCstNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + returnTypeCstNode.hashCode()
    return result
  }

  override fun toString() = StringBuilder().apply {
    append("fun ")
    append(returnTypeCstNode)
    append(" ")
    append(name)
    append(parameters.joinToString(separator = ", ", prefix = "(", postfix = ")"))
  }.toString()
}