package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class StringNode(val parts: List<ExpressionNode>): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return parts.joinToString(separator = " + ")
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    parts.forEach { it.accept(visitor) }
  }
}