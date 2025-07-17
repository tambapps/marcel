package com.tambapps.marcel.semantic.ast

infix fun IdentifiableAstNode?.eq(other: IdentifiableAstNode?) = when {
  this == null && other == null -> true
  this == null || other == null -> false
  else -> this.isSemanticEqualTo(other)
}

infix fun IdentifiableAstNode?.notEq(other: IdentifiableAstNode?) = !eq(other)

infix fun List<IdentifiableAstNode>.eq(other: List<IdentifiableAstNode>): Boolean {
  if (this.size != other.size) return false
  for (i in this.indices) {
    if (!this[i].isSemanticEqualTo(other[i])) return false
  }
  return true
}

infix fun List<IdentifiableAstNode>.notEq(other: List<IdentifiableAstNode>) = !eq(other)

interface IdentifiableAstNode: AstNode {

  /**
   * Checks whether this node is considered equal (in terms of semantic) to the other
   */
  fun isSemanticEqualTo(other: AstNode): Boolean

}