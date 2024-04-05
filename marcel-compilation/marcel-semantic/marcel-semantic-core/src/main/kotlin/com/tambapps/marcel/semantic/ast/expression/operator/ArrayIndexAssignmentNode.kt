package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor

class ArrayIndexAssignmentNode(
  val owner: ExpressionNode,
  val indexExpr: ExpressionNode,
  val expression: ExpressionNode,
  node: CstNode
) : AbstractExpressionNode(owner.type.asArrayType.elementsType, node) {
  val arrayType = owner.type.asArrayType
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}