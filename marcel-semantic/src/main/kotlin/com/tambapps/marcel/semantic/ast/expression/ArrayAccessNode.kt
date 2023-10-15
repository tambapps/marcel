package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor

class ArrayAccessNode(
  val owner: ExpressionNode,
  val indexNode: ExpressionNode,
  node: CstNode) :
  AbstractExpressionNode(owner.type.asArrayType.elementsType, node) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}