package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class LiteralListNode(val elements: List<ExpressionNode>): ExpressionNode {

  override var type = JavaType.of(ArrayList::class.java)

  val elementsType: JavaType
    get() = JavaType.commonType(elements)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

}