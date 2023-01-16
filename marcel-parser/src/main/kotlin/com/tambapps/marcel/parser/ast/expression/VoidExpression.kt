package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class VoidExpression: ExpressionNode {
  override val type = JavaType.void

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}