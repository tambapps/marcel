package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode {
  // for now only ints are handled
  override val type = JavaPrimitiveType.INT

  constructor(name: String): this(name, mutableListOf())

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}