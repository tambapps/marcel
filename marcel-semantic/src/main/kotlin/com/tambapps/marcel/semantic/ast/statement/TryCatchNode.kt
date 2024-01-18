package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode

class TryCatchNode(
  node: CstNode,
  val tryStatementNode: StatementNode,
  // TODO use below
  // the finally statement to execute at the end of the try block but for which we don't catch exceptions
  val successFinallyNode: StatementNode?,
  val catchNodes: List<CatchNode>,
  val finallyNode: CatchNode?, // TODO remove finallyNode. asm only support try/catch

  ) : AbstractStatementNode(node) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}