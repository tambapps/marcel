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

data class LongConstantNode(val value: Long): ExpressionNode {

  override val type = JavaType.long
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return value.toString()
  }
}

data class FloatConstantNode(val value: Float): ExpressionNode {

  override val type = JavaType.float
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return value.toString()
  }
}

data class DoubleConstantNode(val value: Double): ExpressionNode {

  override val type = JavaType.double
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

class NullValueNode(override val type: JavaType): ExpressionNode {

  // treating null as void. Might not be appropriate in some cases though
  constructor(): this(JavaType.void)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun equals(other: Any?): Boolean {
    return other is NullValueNode
  }

  override fun hashCode(): Int {
    return 0
  }
}