package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.IntRange

class RangeNode(val from: ExpressionNode, val to: ExpressionNode,
  val fromExclusive: Boolean, val toExclusive: Boolean): ExpressionNode {

  // TODO change when supporting other primitive ranges
  override val type: JavaType
    get() = JavaType.of(IntRange::class.java)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    from.accept(visitor)
    to.accept(visitor)
  }

}