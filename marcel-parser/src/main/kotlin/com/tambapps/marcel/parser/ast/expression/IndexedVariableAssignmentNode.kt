package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope

open class IndexedVariableAssignmentNode(override var scope: Scope, val indexedReference: IndexedReferenceExpression,
                                         val expression: ExpressionNode): ExpressionNode, ScopedNode<Scope> {
  override val type: JavaType
    get() = expression.type

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "$indexedReference = $expression"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is IndexedVariableAssignmentNode) return false

    if (indexedReference != other.indexedReference) return false
    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    var result = indexedReference.hashCode()
    result = 31 * result + expression.hashCode()
    return result
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    expression.accept(visitor)
  }
}