package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable

class TryNode(
  node: CstNode,
  val tryStatementNode: StatementNode,
  val catchNodes: List<CatchNode>,
  val finallyNode: FinallyNode?,

  ) : AbstractStatementNode(node) {
  data class CatchNode(
    val throwableTypes: List<JavaType>,
    val throwableVariable: LocalVariable,
    val statement: StatementNode,
  )

  data class FinallyNode(
    val throwableVariable: LocalVariable,
    val statement: StatementNode,)

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}