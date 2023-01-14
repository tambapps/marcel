package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.StatementVisitor

class ExpressionStatementNode(val expression: ExpressionNode): StatementNode {

  override fun accept(mv: StatementVisitor) {
    mv.visit(this)
  }

}