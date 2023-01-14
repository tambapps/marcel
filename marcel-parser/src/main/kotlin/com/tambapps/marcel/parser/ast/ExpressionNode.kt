package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.ExpressionVisitor

interface ExpressionNode: AstNode {
  fun accept(expressionVisitor: ExpressionVisitor)

}