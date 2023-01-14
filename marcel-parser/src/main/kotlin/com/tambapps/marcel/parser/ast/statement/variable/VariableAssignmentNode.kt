package com.tambapps.marcel.parser.ast.statement.variable

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

// TODO move this under expression.variable package
open class VariableAssignmentNode(val name: String, val expression: ExpressionNode): ExpressionNode {
  override val type: JavaType
    get() = expression.type

  override fun accept(mv: ExpressionVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "$name = $expression"
  }
}