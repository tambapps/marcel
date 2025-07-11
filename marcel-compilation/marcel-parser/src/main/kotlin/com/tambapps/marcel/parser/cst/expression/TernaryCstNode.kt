package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TernaryCstNode(
  val testExpressionNode: ExpressionCstNode,
  val trueExpressionNode: ExpressionCstNode,
  val falseExpressionNode: ExpressionCstNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = "$testExpressionNode ? $trueExpressionNode : $falseExpressionNode"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TernaryCstNode) return false

    if (testExpressionNode != other.testExpressionNode) return false
    if (trueExpressionNode != other.trueExpressionNode) return false
    if (falseExpressionNode != other.falseExpressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = testExpressionNode.hashCode()
    result = 31 * result + trueExpressionNode.hashCode()
    result = 31 * result + falseExpressionNode.hashCode()
    return result
  }
}