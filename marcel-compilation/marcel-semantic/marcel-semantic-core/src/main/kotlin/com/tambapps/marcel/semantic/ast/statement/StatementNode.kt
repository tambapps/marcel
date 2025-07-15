package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.IdentifiableAstNode
import com.tambapps.marcel.semantic.ast.visitor.IsEqualVisitor
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor

interface StatementNode : AstNode, IdentifiableAstNode {

  fun <T> accept(visitor: StatementNodeVisitor<T>): T

  /**
   * Whether this statement represents an empty statement, does nothing, no instruction
   */
  val isEmpty: Boolean get() = false

  override fun isEqualTo(other: AstNode) = accept(IsEqualVisitor(other))

}