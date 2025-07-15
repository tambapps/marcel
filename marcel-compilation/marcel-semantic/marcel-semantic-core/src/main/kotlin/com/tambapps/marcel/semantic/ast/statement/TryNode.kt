package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable

class TryNode(
  node: CstNode,
  var tryStatementNode: StatementNode,
  val catchNodes: List<CatchNode>,
  val finallyNode: FinallyNode?,
  ) : AbstractStatementNode(node) {
  data class CatchNode(
    val throwableTypes: List<JavaType>,
    val throwableVariable: LocalVariable,
    var statement: StatementNode,
  )

  data class FinallyNode(
    val throwableVariable: LocalVariable,
    var statement: BlockStatementNode,
    val returnVariable: LocalVariable?,
  )

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

  override val isEmpty get() = tryStatementNode.isEmpty
}