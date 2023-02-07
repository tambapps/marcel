package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Variable

// can be a class or variable reference
class ReferenceExpression(override var scope: Scope, val name: String): ExpressionNode, ScopedNode<Scope> {

  companion object {
    fun thisRef(scope: Scope): ReferenceExpression {
      return ReferenceExpression(scope, "this")
    }
  }
  val variable: Variable
    get() = scope.findVariable(name)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return name
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ReferenceExpression

    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }
}