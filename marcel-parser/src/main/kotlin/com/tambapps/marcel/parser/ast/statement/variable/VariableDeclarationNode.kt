package com.tambapps.marcel.parser.ast.statement.variable

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.StatementVisitor

class VariableDeclarationNode(override val type: JavaType, name: String, expressionNode: ExpressionNode): VariableAssignmentNode(name, expressionNode), StatementNode {

  override fun accept(mv: StatementVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "${type.className} $name = $expressionNode;"
  }
}