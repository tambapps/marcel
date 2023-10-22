package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable

class IncrLocalVariableNode(
  node: CstNode,
  val variable: LocalVariable,
  val amount: Int,
  val returnValueBefore: Boolean,
) : AbstractExpressionNode(variable.type, node) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}