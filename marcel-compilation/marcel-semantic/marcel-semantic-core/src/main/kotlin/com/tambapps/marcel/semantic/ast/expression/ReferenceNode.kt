package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.Variable
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField

class ReferenceNode(
  override val owner: ExpressionNode? = null,
  override var variable: Variable, token: LexToken
) : AbstractExpressionNode(token),
  AstVariableNode, OwnableAstNode {

  override val type = variable.type
  override val nullness: Nullness
    get() = variable.nullness

  override fun withOwner(owner: ExpressionNode) =
    ReferenceNode(owner, variable, token)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReferenceNode) return false

    if (variable != other.variable) return false

    return true
  }

  override fun hashCode(): Int {
    return variable.hashCode()
  }

  override fun toString() = StringBuilder().apply {
    if ((variable as? MarcelField)?.isMarcelStatic == true) {
      append((variable as MarcelField).owner.simpleName)
      append(".")
    } else if (owner != null) {
      append(owner)
      append(".")
    }
    append(variable.name)
  }.toString()

}