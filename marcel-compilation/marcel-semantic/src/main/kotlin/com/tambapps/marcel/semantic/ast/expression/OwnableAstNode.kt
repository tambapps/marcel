package com.tambapps.marcel.semantic.ast.expression

/**
 * Node that can have an owner
 */
interface OwnableAstNode: ExpressionNode {
  val owner: ExpressionNode?

  fun withOwner(owner: ExpressionNode): OwnableAstNode

}