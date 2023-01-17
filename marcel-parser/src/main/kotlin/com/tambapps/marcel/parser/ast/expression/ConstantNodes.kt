package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

data class IntConstantNode(val value: Int): ExpressionNode {

  override val type = JavaType.int
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return value.toString()
  }
}

data class StringConstantNode(val value: String): ExpressionNode {
  override val type = JavaType.STRING

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "\"$value\""
  }
}