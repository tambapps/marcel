package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

data class IntConstantNode(val value: Int): ExpressionNode {
  // for now only ints are handled
  override val type = JavaPrimitiveType.INT
  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }

}