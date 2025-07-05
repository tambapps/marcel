package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class TernaryNode(
  val testExpressionNode: ExpressionNode,
  val trueExpressionNode: ExpressionNode,
  val falseExpressionNode: ExpressionNode,
  node: CstNode
) : AbstractExpressionNode(node) {

  override val type = JavaType.commonType(
    trueExpressionNode,
    falseExpressionNode
  )

  override val nullness: Nullness
    get() = Nullness.of(trueExpressionNode, falseExpressionNode)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}