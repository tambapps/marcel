package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

open class StatementsComposer(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY
) :
  ExpressionComposer(tokenStart, tokenEnd) {
    private val statements = mutableListOf<StatementCstNode>()

  companion object {
    fun compose(tokenStart: LexToken = LexToken.DUMMY, tokenEnd: LexToken = LexToken.DUMMY, block: StatementsComposer.() -> Unit): BlockCstNode {
      val composer = StatementsComposer(tokenStart, tokenEnd)
      composer.block()
      return composer.asBlock()
    }
  }
  fun stmt(expr: ExpressionCstNode) = add(
    ExpressionStatementCstNode(expr)
  )

  fun varDecl(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?, isNullable: Boolean = false) = add(
    VariableDeclarationCstNode(typeNode,
      LexToken.dummy(name), expr, isNullable, null, tokenStart, tokenEnd)
  )

  fun returnNode(expr: ExpressionCstNode? = null) = add(ReturnCstNode(expressionNode = expr, tokenStart = tokenStart, tokenEnd = tokenEnd))

  fun asBlock() = BlockCstNode(statements, null, tokenStart, tokenEnd)
  fun asStmt() = when (statements.size) {
    1 -> statements[0]
    else -> asBlock()
  }

  private fun add(statement: StatementCstNode): StatementCstNode = statement.also { statements.add(it) }

}