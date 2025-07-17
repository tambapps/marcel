package com.tambapps.marcel.parser.cst

infix fun IdentifiableCstNode?.eq(other: IdentifiableCstNode?) = when {
  this == null && other == null -> true
  this == null || other == null -> false
  else -> this.isSyntaxEqualTo(other)
}

infix fun IdentifiableCstNode?.notEq(other: IdentifiableCstNode?) = !eq(other)

infix fun List<IdentifiableCstNode>.eq(other: List<IdentifiableCstNode>): Boolean {
  if (this.size != other.size) return false
  for (i in this.indices) {
    if (!this[i].isSyntaxEqualTo(other[i])) return false
  }
  return true
}

infix fun List<IdentifiableCstNode>.notEq(other: List<IdentifiableCstNode>) = !eq(other)

interface IdentifiableCstNode: CstNode {

  /**
   * Checks whether this node is considered equal (in terms of syntax) to the other
   */
  fun isSyntaxEqualTo(other: CstNode): Boolean

}