package com.tambapps.marcel.parser.cst.expression.operator

import com.tambapps.marcel.parser.cst.expression.CstExpressionNode


interface BinaryOperatorCstNode: CstExpressionNode {

  val leftOperand: CstExpressionNode
  val rightOperand: CstExpressionNode
}