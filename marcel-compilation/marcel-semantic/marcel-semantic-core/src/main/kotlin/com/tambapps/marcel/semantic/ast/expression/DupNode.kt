package com.tambapps.marcel.semantic.ast.expression

/**
 * Node for a DUP asm instruction.
 * This node is handy for ElvisOperator, where we can DUP a value instead of assigning it into a local variable
 *
 */
class DupNode(val expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(expression.type, expression.tokenStart, expression.tokenEnd) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}