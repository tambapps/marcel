package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.ExpressionVisitor

data class IntConstantNode(val value: Int): ExpressionNode {
  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }

}