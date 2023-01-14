package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

data class IntConstantNode(val value: Int): ExpressionNode {
  // for now only ints are handled
  override val type = JavaPrimitiveType.INT
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return value.toString()
  }
}