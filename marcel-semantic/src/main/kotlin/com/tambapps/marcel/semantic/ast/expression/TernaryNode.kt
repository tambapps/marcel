package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class TernaryNode(
  val testExpressionNode: ExpressionNode,
  val trueExpressionNode: ExpressionNode,
  val falseExpressionNode: ExpressionNode,
  node: CstNode) :
  AbstractExpressionNode(JavaType.commonType(trueExpressionNode, falseExpressionNode), node) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}