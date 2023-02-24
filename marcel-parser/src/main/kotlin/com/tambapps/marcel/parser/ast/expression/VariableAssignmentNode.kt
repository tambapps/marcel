package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope

open class VariableAssignmentNode(token: LexToken, override var scope: Scope, val name: String, val expression: ExpressionNode): AbstractExpressionNode(token), ScopedNode<Scope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$name = $expression"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VariableAssignmentNode) return false

    if (name != other.name) return false
    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + expression.hashCode()
    return result
  }

}