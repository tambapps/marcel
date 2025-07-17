package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode

open class AstStatementScope(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY) :
  AstExpressionScope(tokenStart, tokenEnd) {

  fun stmt(expr: ExpressionNode) = onStatementComposed(ExpressionStatementNode(expr))

  fun returnStmt(expr: ExpressionNode? = null) = onStatementComposed(ReturnStatementNode(expr, tokenStart, tokenEnd))

  protected open fun <T: StatementNode> onStatementComposed(statement: T): T = statement
}