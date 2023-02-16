package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.IntRange

class RangeNode(val from: ExpressionNode, val to: ExpressionNode,
  val fromExclusive: Boolean, val toExclusive: Boolean): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


}