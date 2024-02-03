package com.tambapps.marcel.semantic.ast.expression

/**
 * Node that can have an owner
 */
interface OwnableAstNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode {
  val owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode?

  fun withOwner(owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode): OwnableAstNode

}