package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor

/**
 * Return node
 */
class ReturnStatementNode(
  expressionNode: ExpressionNode?,
  tokenStart: LexToken, tokenEnd: LexToken
) : ExpressionStatementNode(expressionNode ?: VoidExpressionNode(tokenStart), tokenStart, tokenEnd) {

  constructor(expressionNode: ExpressionNode) : this(
    expressionNode,
    expressionNode.tokenStart,
    expressionNode.tokenEnd
  )

  val returnsVoid = expressionNode is VoidExpressionNode
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}