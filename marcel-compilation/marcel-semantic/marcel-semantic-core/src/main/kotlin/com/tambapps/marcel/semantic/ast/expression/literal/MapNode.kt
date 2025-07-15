package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class MapNode(
  val entries: List<Pair<ExpressionNode, ExpressionNode>>,
  node: CstNode
) :
  AbstractExpressionNode(node) {

  override val type = JavaType.Map

  override val nullness: Nullness
    get() = Nullness.NOT_NULL
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}