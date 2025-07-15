package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * Node for a DUP asm instruction.
 * This node is handy for ElvisOperator, where we can DUP a value instead of assigning it into a local variable
 *
 */
class DupNode(val expression: ExpressionNode) :
  AbstractExpressionNode(
    expression.tokenStart,
    expression.tokenEnd
  ) {
    override val type = expression.type
    override val nullness: Nullness
    get() = expression.nullness
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}