package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode

class TryCatchNode(
  node: CstNode,
  val tryStatementNode: StatementNode,
  val catchNodes: List<CatchNode>,
  val finallyNode: CatchNode?,

  ) : AbstractStatementNode(node) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}