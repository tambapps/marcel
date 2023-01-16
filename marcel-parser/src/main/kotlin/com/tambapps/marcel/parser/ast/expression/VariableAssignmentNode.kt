package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

open class VariableAssignmentNode(val name: String, val expression: ExpressionNode): ExpressionNode {
  override val type: JavaType
    get() = expression.type

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "$name = $expression"
  }
}