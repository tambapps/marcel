package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode

class VariableDeclarationNode(override val type: JavaType, name: String,  expression: ExpressionNode): VariableAssignmentNode(name, expression), StatementNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "${type.className} $name = $expression;"
  }
}