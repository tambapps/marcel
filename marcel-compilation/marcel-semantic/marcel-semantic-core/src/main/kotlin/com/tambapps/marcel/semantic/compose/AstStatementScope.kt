package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode

open class AstStatementScope(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY) :
  AstExpressionScope(tokenStart, tokenEnd) {


  fun stmt(expr: ExpressionNode) = ExpressionStatementNode(expr)

  protected open fun <T: StatementCstNode> onStatementComposed(statement: T): T = statement
}