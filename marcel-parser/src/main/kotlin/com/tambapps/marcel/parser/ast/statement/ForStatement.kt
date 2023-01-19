package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression

class ForStatement(val initStatement: StatementNode,
                   val endCondition: BooleanExpressionNode, val iteratorStatement: StatementNode, val statement: StatementNode): StatementNode {

  override val expression = VoidExpression()


  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

}