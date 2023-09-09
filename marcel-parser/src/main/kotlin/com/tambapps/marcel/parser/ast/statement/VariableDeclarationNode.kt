package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.scope.Scope

open class VariableDeclarationNode constructor(token: LexToken, scope: Scope, val type: JavaType, name: String, val isFinal: Boolean, expression: ExpressionNode?)
  : VariableAssignmentNode(token, scope, name, expression ?: type.defaultValueExpression), StatementNode {

    fun withExpression(expression: ExpressionNode) =
      VariableDeclarationNode(token, scope, type, name, isFinal, expression)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "${type.className} $name = $expression;"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VariableDeclarationNode) return false
    if (!super.equals(other)) return false

    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}