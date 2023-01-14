package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.ExpressionVisitor

class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode {

  constructor(name: String): this(name, mutableListOf())

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}