package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class TernaryNode(
  val testExpressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val trueExpressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val falseExpressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  node: CstNode) :
  com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.commonType(trueExpressionNode, falseExpressionNode), node) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}