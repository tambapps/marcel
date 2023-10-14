package com.tambapps.marcel.parser.cst.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode

abstract class AbstractOperatorNode(
  override val leftOperand: CstExpressionNode,
  override val rightOperand: CstExpressionNode,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd), BinaryOperatorCstNode {
}