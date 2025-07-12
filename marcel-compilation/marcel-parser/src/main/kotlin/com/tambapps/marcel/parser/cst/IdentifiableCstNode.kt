package com.tambapps.marcel.parser.cst

interface IdentifiableCstNode: CstNode {

  // useful for testing
  fun isEqualTo(node: CstNode): Boolean

  companion object {
    fun isEqualTo(i1: IdentifiableCstNode?, iE: IdentifiableCstNode?) = when {
      i1 == null && iE == null -> true
      i1 == null || iE == null -> false
      else -> i1.isEqualTo(iE)
    }
    fun isEqualTo(l1: List<IdentifiableCstNode>, l2: List<IdentifiableCstNode>): Boolean {
      if (l1.size != l2.size) return false
      for (i in l1.indices) {
        if (!l1[i].isEqualTo(l2[i])) return false
      }
      return true
    }
  }
}