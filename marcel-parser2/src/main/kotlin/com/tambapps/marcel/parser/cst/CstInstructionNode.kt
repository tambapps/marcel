package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor

interface CstInstructionNode: CstNode {
  fun <T> accept(visitor: CstNodeVisitor<T>): T
  fun <T> accept(visitor: ExpressionCstNodeVisitor<T>): T
  fun <T> accept(visitor: StatementCstNodeVisitor<T>): T
}