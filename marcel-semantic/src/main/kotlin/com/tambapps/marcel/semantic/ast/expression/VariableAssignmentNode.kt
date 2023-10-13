package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.variable.Variable

class VariableAssignmentNode(
  val variable: Variable,
  val expression: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(variable.type, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}