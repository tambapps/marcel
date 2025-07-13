package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

/**
 * Scope of a statement composition
 */
open class StatementScope(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY,
  parent: CstNode? = null
) :
  ExpressionScope(tokenStart, tokenEnd, parent) {

  fun stmt(expr: ExpressionCstNode) = onStatementComposed(
    ExpressionStatementCstNode(expr)
  )

  fun block(block: StatementScope.() -> Unit) = BlockCstNode(
    BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).statements,
    null, tokenStart, tokenEnd
  )

  fun varDecl(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?, isNullable: Boolean = false) = onStatementComposed(
    VariableDeclarationCstNode(typeNode,
      LexToken.dummy(name), expr, isNullable, parent, tokenStart, tokenEnd)
  )

  fun returnStmt(expr: ExpressionCstNode? = null) = onStatementComposed(ReturnCstNode(expressionNode = expr, tokenStart = tokenStart, tokenEnd = tokenEnd))

  fun ifStmt(
    condition: ExpressionCstNode, compose: IfElseStatementScope.() -> Unit
  ) = onStatementComposed(IfElseStatementScope(condition).apply(compose).asIfStmt())

  protected open fun <T: StatementCstNode> onStatementComposed(statement: T): T = statement

}

class BlockStatementScope constructor(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY,
  parent: CstNode? = null,
  val statements: MutableList<StatementCstNode> = mutableListOf(),
) :
  StatementScope(tokenStart, tokenEnd, parent) {

  override fun <T: StatementCstNode> onStatementComposed(statement: T): T = statement.also { statements.add(it) }

  fun asBlock() = BlockCstNode(statements, parent, tokenStart, tokenEnd)

}

class IfElseStatementScope(
  private val condition: ExpressionCstNode,
  private val tokenStart: LexToken = LexToken.DUMMY,
  private val tokenEnd: LexToken = LexToken.DUMMY,
  private val parent: CstNode? = null
) {
  private var trueStatement: StatementCstNode? = null
  private var falseStatement: StatementCstNode? = null

  fun trueStmt(compose: StatementScope.() -> StatementCstNode) {
    trueStatement = compose.invoke(StatementScope(tokenStart, tokenEnd, parent))
  }

  fun falseStmt(compose: StatementScope.() -> StatementCstNode) {
    falseStatement = compose.invoke(StatementScope(tokenStart, tokenEnd, parent))
  }

  fun trueBlock(compose: BlockStatementScope.() -> Unit) {
    trueStatement = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  fun falseBlock(compose: BlockStatementScope.() -> Unit) {
    falseStatement = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  fun asIfStmt(): IfStatementCstNode {
    val trueStatement = this.trueStatement ?: throw IllegalStateException("true statement must be set")
    return IfStatementCstNode(condition, trueStatement, falseStatement, parent, tokenStart, tokenEnd)
  }
}