package com.tambapps.marcel.parser.ast.statement.variable

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.StatementVisitor

class VariableDeclarationNode(val type: JavaType, val name: String, val expressionNode: ExpressionNode): StatementNode {

  override fun accept(mv: StatementVisitor) {
    mv.visit(this)
  }
}