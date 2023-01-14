package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

interface ExpressionNode: AstNode {

  val type: JavaType
  fun accept(expressionVisitor: ExpressionVisitor)

}