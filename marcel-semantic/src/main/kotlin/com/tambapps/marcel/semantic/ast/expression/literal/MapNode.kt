package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class MapNode(val entries: List<Pair<ExpressionNode, ExpressionNode>>, node: CstNode) :
  AbstractExpressionNode(JavaType.Map, node) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}