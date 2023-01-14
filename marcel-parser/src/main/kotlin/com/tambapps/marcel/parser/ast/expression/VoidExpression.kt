package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class VoidExpression: ExpressionNode {
  override val type = JavaPrimitiveType.VOID

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}