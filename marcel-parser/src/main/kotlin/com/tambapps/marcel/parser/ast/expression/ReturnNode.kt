package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ResolvableNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InMethodScope
import com.tambapps.marcel.parser.scope.Scope

class ReturnNode(override val expression: ExpressionNode) : StatementNode, ResolvableNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun resolve(scope: Scope) {
    if (scope !is InMethodScope) {
      throw SemanticException("Tried to return a value not from a method")
    }
    val method = scope.currentMethod
    if (method.returnType != expression.type) {
      throw SemanticException("Cannot return ${expression.type} when return type is ${method.returnType}")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReturnNode) return false

    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    return expression.hashCode()
  }
}