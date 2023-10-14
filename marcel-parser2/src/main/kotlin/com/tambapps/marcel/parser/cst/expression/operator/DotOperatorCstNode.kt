package com.tambapps.marcel.parser.cst.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class DotOperatorCstNode(
  leftOperand: CstExpressionNode,
  rightOperator: CstExpressionNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractOperatorNode(leftOperand, rightOperator, parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)
}