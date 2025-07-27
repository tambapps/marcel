package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class TernaryNode(
  val testExpressionNode: ExpressionNode,
  val trueExpressionNode: ExpressionNode,
  val falseExpressionNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken,
) : AbstractExpressionNode(tokenStart, tokenEnd) {

  constructor(testExpressionNode: ExpressionNode, trueExpressionNode: ExpressionNode, falseExpressionNode: ExpressionNode, node: CstNode)
      : this(testExpressionNode, trueExpressionNode, falseExpressionNode, node.tokenStart, node.tokenEnd)

  override val type = JavaType.commonType(
    trueExpressionNode,
    falseExpressionNode
  )

  override val nullness: Nullness
    get() = Nullness.of(trueExpressionNode, falseExpressionNode)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}