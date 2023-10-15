package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

class PlusNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override val type: JavaType
    get() = super.type.asPrimitiveType // should always be a primitive
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}