package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ContinueCstNode(parent: CstNode?, token: LexToken) : AbstractStatementCstNode(parent, token) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ContinueCstNode) return false
    return true
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}