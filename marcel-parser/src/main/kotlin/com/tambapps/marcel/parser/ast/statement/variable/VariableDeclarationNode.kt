package com.tambapps.marcel.parser.ast.statement.variable

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ExpressionVisitor

class VariableDeclarationNode(override val type: JavaType, name: String,  expression: ExpressionNode): VariableAssignmentNode(name, expression), StatementNode {

  override fun accept(mv: ExpressionVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "${type.className} $name = $expression;"
  }
}