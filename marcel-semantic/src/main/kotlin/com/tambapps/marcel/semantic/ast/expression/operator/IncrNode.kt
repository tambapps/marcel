package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.variable.Variable

class IncrNode(
  node: CstNode,
  val variable: Variable,
  val owner: ExpressionNode?,
  val incrExpression: ExpressionNode,
  val returnValueBefore: Boolean,
) : AbstractExpressionNode(variable.type, node) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}