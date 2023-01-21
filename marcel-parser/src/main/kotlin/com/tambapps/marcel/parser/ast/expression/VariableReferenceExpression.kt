package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.LocalVariable

// can be a class or variable reference
class VariableReferenceExpression(override val scope: Scope, val name: String): ExpressionNode, ScopedNode<Scope> {
  override val type: JavaType
    get() = try {
        scope.getLocalVariable(name).type
      } catch (e: SemanticException) {
        scope.getTypeOrNull(name) ?: throw e
      }

  val variable: LocalVariable
    get() = scope.getLocalVariable(name)
  val index: Int
    get() = scope.getLocalVariableIndex(name)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return name
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as VariableReferenceExpression

    if (name != other.name) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}