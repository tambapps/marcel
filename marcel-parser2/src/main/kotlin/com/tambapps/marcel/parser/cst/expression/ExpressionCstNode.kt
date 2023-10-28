package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.CstNode

interface ExpressionCstNode: CstNode {

  /**
   * Whether this expression is used as a Statement, in an ExpressionStatementNode.
   * Useful for Semantic Analysis
   */
  var isStatement: Boolean

  fun <T> accept(visitor: ExpressionCstNodeVisitor<T>): T
}