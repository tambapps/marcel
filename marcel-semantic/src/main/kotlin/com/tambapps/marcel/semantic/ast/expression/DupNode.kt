package com.tambapps.marcel.semantic.ast.expression

/**
 * Node for a DUP asm instruction.
 * This node is handy for ElvisOperator, where we can DUP a value instead of assigning it into a local variable
 *
 */
class DupNode(val expression: ExpressionNode) : AbstractExpressionNode(expression.type, expression.tokenStart, expression.tokenEnd) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}