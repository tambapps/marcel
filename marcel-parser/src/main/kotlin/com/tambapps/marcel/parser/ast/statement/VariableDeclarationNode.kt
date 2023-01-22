package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.scope.Scope

open class VariableDeclarationNode(scope: Scope, override val type: JavaType, name: String,  expression: ExpressionNode): VariableAssignmentNode(scope, name, expression), StatementNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

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