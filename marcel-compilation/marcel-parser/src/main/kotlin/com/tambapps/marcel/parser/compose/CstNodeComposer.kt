package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

object CstNodeComposer {

  fun composeExpr(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    composer: ExpressionScope.() -> ExpressionCstNode
  ) = composer.invoke(ExpressionScope(tokenStart, tokenEnd))

  fun composeStmt(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    block: StatementScope.() -> StatementCstNode
  ) = block.invoke(StatementScope(tokenStart, tokenEnd))

  fun composeBlock(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    block: StatementScope.() -> Unit
  ) = BlockStatementScope(tokenStart, tokenEnd).apply(block).asBlock()

}