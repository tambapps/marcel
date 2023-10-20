package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class NotNode(val expressionNode: ExpressionNode, node: CstNode) :
  AbstractExpressionNode(JavaType.boolean, node) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}