package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.ExpressionVisitor

class TernaryNode(boolExpression: ExpressionNode,
                  trueExpression: ExpressionNode,
                  falseExpression: ExpressionNode): ExpressionNode {
  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }

}