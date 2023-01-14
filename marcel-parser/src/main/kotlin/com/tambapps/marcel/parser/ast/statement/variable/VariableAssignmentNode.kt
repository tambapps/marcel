package com.tambapps.marcel.parser.ast.statement.variable

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

open class VariableAssignmentNode(val name: String, val expressionNode: ExpressionNode): ExpressionNode {
  override val type: JavaType
    get() = expressionNode.type

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }

  override fun toString(): String {
    return "$name = $expressionNode"
  }
}