package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

/**
 * Scope of a statement composition
 */
open class StatementScope(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY
) :
  ExpressionScope(tokenStart, tokenEnd) {

  fun stmt(expr: ExpressionCstNode) = onStatementComposed(
    ExpressionStatementCstNode(expr)
  )

  fun block(block: StatementScope.() -> Unit) = BlockCstNode(
    BlockStatementScope().apply(block).statements,
    null, tokenStart, tokenEnd
  )

  fun varDecl(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?, isNullable: Boolean = false) = onStatementComposed(
    VariableDeclarationCstNode(typeNode,
      LexToken.dummy(name), expr, isNullable, null, tokenStart, tokenEnd)
  )

  fun returnNode(expr: ExpressionCstNode? = null) = onStatementComposed(ReturnCstNode(expressionNode = expr, tokenStart = tokenStart, tokenEnd = tokenEnd))

  protected open fun onStatementComposed(statement: StatementCstNode): StatementCstNode = statement

}

class BlockStatementScope(tokenStart: LexToken = LexToken.DUMMY, tokenEnd: LexToken = LexToken.DUMMY) :
  StatementScope(tokenStart, tokenEnd) {
  internal val statements = mutableListOf<StatementCstNode>()

  override fun onStatementComposed(statement: StatementCstNode): StatementCstNode = statement.also { statements.add(it) }

  fun asBlock() = BlockCstNode(statements, null, tokenStart, tokenEnd)

}