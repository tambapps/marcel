package com.tambapps.marcel.parser.ast.variable

import com.tambapps.marcel.parser.ast.ExpressionNode
import com.tambapps.marcel.parser.ast.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.StatementVisitor

class VariableDeclarationNode(val type: JavaType, val name: String, val expressionNode: ExpressionNode): StatementNode {

  override fun accept(mv: StatementVisitor) {
    mv.visit(this)
  }
}