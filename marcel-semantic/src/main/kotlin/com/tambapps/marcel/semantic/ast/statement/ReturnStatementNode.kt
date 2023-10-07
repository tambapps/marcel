package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

/**
 * Return node
 */
class ReturnStatementNode(
  expressionNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) : ExpressionStatementNode(expressionNode, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}