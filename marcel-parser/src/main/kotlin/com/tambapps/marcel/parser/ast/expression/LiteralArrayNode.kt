package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaArrayType

// var type useful for emptyArrayNode
open class LiteralArrayNode constructor(var type: JavaArrayType?, val elements: List<ExpressionNode>): ExpressionNode {

  constructor(elements: List<ExpressionNode>): this(null, elements)
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    elements.forEach { it.accept(visitor) }
  }
}

class EmptyArrayNode(type: JavaArrayType): LiteralArrayNode(type, emptyList())