package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class MapNode(val entries: List<Pair<com.tambapps.marcel.semantic.ast.expression.ExpressionNode, com.tambapps.marcel.semantic.ast.expression.ExpressionNode>>, node: CstNode) :
  com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.Map, node) {

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}