package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.visitor.StatementVisitor

class ExpressionStatementNode(val expression: ExpressionNode): StatementNode {

  override fun accept(mv: StatementVisitor) {
    mv.visit(this)
  }

}