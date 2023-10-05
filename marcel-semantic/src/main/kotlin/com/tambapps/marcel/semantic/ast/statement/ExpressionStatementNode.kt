package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class ExpressionStatementNode(
  val expressionNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) : AbstractStatementNode(tokenStart, tokenEnd) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}