package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.ast.AstNode

interface StatementNode : AstNode {

  fun <T> accept(visitor: StatementNodeVisitor<T>): T

  /**
   * Whether this statement represents an empty statement, does nothing, no instruction
   */
  val isEmpty: Boolean get() = false
}