package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Variable

// can be a class or variable reference
class ReferenceExpression(override val scope: Scope, val name: String): ExpressionNode, ScopedNode<Scope> {
  override val type: JavaType
    get() = try {
        scope.findVariable(name).type
      } catch (e: SemanticException) {
        // for static function calls
        scope.getTypeOrNull(name) ?: throw e
      }

  val variable: Variable
    get() = scope.findVariable(name)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return name
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ReferenceExpression

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