package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaType

open class LiteralArrayNode(val elements: List<ExpressionNode>): ExpressionNode {

  override val type: JavaArrayType get() =  JavaType.arrayType(elementsType)

  val elementsType: JavaType
    get() = JavaType.commonType(elements)

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    elements.forEach { it.accept(visitor) }
  }
}

class EmptyArrayNode(override val type: JavaArrayType): LiteralArrayNode(emptyList())