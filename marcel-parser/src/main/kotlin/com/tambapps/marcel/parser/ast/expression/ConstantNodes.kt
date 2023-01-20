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
  override val type = JavaType.String

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "\"$value\""
  }
}

data class BooleanConstantNode(val value: Boolean): ExpressionNode {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override val type = JavaType.boolean
}

class NullValueNode(): ExpressionNode {

  // treating null as void
  override val type = JavaType.void

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

}