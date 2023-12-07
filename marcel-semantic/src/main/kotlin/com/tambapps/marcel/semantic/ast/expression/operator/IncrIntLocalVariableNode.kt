package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable

// only for int local variables
class IncrIntLocalVariableNode constructor(
  node: CstNode,
  override var variable: Variable, // MUST BE A LOCAL VARIABLE
  val amount: Int,
  val returnValueBefore: Boolean,
) : AbstractExpressionNode(variable.type, node), AstVariableNode {
  val localVariable get() = variable as LocalVariable
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}