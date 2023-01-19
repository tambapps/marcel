package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression

class ForStatement(val initStatement: StatementNode,
  val endCondition: ExpressionNode, val iteratorStatement: StatementNode, val statement: StatementNode): StatementNode {

  override val expression = VoidExpression()


  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

}