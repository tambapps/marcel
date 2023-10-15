package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class ArrayIndexAssignmentNode(
  val arrayExpr: ExpressionNode,
  val indexExpr: ExpressionNode,
  val expression: ExpressionNode,
  node: CstNode
) : AbstractExpressionNode(arrayExpr.type.asArrayType.elementsType, node) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}