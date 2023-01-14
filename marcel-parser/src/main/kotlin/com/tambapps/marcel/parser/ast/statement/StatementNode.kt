package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.ExpressionVisitor

interface StatementNode: AstNode {
  val type: JavaType
    get() = expression.type

  val expression: ExpressionNode

  fun accept(mv: ExpressionVisitor)

}