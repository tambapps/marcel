package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

/**
 * Object allowing to compose CST expressions or statements
 */
object CstInstructionComposer {

  inline fun expr(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    composer: ExpressionScope.() -> ExpressionCstNode
  ) = composer.invoke(ExpressionScope(tokenStart, tokenEnd))

  inline fun stmt(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    block: StatementScope.() -> StatementCstNode
  ) = block.invoke(StatementScope(tokenStart, tokenEnd))

  inline fun block(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    block: StatementScope.() -> Unit
  ) = BlockStatementScope(tokenStart, tokenEnd).apply(block).asBlock()

}