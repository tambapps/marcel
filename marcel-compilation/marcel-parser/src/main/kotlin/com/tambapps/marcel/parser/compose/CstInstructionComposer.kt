package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

/**
 * Object allowing to compose CST expressions or statements
 */
object CstInstructionComposer {

  inline fun expr(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    parent: CstNode? = null,
    composer: ExpressionScope.() -> ExpressionCstNode
  ) = composer.invoke(ExpressionScope(tokenStart, tokenEnd, parent))

  inline fun stmt(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    parent: CstNode? = null,
    block: StatementScope.() -> StatementCstNode
  ) = block.invoke(StatementScope(tokenStart, tokenEnd, parent))

  inline fun block(
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    parent: CstNode? = null,
    block: StatementScope.() -> Unit
  ) = BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).asBlock()

}