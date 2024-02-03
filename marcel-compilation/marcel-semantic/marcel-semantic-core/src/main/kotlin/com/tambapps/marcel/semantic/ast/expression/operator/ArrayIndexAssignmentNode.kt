package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor

class ArrayIndexAssignmentNode(
  val owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val indexExpr: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val expression: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  node: CstNode
) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(owner.type.asArrayType.elementsType, node) {
  val arrayType = owner.type.asArrayType
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}