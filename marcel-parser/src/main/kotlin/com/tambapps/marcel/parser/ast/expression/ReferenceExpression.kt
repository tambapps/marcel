package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Variable

// can be a class or variable reference
class ReferenceExpression constructor(token: LexToken, override var scope: Scope, val name: String): AbstractExpressionNode(token), ScopedNode<Scope> {

  companion object {
    fun thisRef(scope: Scope): ThisReference {
      return ThisReference(LexToken.dummy(), scope)
    }
    fun superRef(scope: Scope): SuperReference {
      return SuperReference(LexToken.dummy(), scope)
    }
  }
  val variable: Variable
    get() = scope.findVariableOrThrow(name)
  val variableExists: Boolean
    get() = scope.findVariable(name) != null

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

class ThisReference(token: LexToken, override var scope: Scope): AbstractExpressionNode(token), ScopedNode<Scope> {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "this"
  }
}

class SuperReference(token: LexToken, override var scope: Scope): AbstractExpressionNode(token), ScopedNode<Scope> {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "super"
  }
}