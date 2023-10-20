package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode

class ArrayAccessNode(
  val owner: ExpressionNode,
  val indexNode: ExpressionNode,
  node: CstNode) :
  AbstractExpressionNode(owner.type.asArrayType.elementsType, node) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}