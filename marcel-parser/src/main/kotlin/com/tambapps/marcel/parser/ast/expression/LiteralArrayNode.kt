package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaType

class LiteralArrayNode(val scope: Scope, val elements: List<ExpressionNode>): ExpressionNode {

  override val type: JavaArrayType = JavaType.arrayType(elementsType)

  val elementsType: JavaType
    get() = JavaType.commonType(elements)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

}