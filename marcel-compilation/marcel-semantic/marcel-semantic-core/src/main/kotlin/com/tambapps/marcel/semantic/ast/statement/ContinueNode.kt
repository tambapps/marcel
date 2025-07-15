package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor

class ContinueNode(node: CstNode) : AbstractStatementNode(node) {
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}