package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class StringNode(val parts: List<ExpressionNode>): ExpressionNode {

  override val type = JavaType.String

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return parts.joinToString(separator = " + ")
  }
}