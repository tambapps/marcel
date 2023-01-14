package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

class VoidExpression: ExpressionNode {
  override val type = JavaPrimitiveType.VOID

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}