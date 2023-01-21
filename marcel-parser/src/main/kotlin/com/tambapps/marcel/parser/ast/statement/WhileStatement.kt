package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression

class WhileStatement(val condition: BooleanExpressionNode, val body: BlockNode): StatementNode {

  override val expression = VoidExpression()


  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

}