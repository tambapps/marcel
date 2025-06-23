package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.Nullness

class ArrayIndexAssignmentNode(
  val owner: ExpressionNode,
  val indexExpr: ExpressionNode,
  val expression: ExpressionNode,
  node: CstNode
) : AbstractExpressionNode(owner.type.asArrayType.elementsType, node) {
  val arrayType = owner.type.asArrayType
  override val nullness: Nullness
    get() = if (arrayType.elementsType.primitive) Nullness.NOT_NULL else Nullness.UNKNOWN

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}