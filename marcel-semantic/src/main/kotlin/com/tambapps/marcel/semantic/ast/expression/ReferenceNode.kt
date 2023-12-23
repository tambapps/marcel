package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.variable.Variable

class ReferenceNode constructor(
  override val owner: ExpressionNode? = null,
  override var variable: Variable, token: LexToken
) : AbstractExpressionNode(variable.type, token),
  AstVariableNode, OwnableAstNode {

  override fun withOwner(owner: ExpressionNode) = ReferenceNode(owner, variable, token)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReferenceNode) return false

    if (variable != other.variable) return false

    return true
  }

  override fun hashCode(): Int {
    return variable.hashCode()
  }

  override fun toString(): String {
    return variable.name
  }

}