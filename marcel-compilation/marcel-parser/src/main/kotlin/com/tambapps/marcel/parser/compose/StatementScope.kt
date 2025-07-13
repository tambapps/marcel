package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode

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

  fun breakStmt() = onStatementComposed(BreakCstNode(parent, tokenStart))
  fun continueStmt() = onStatementComposed(ContinueCstNode(parent, tokenStart))
  fun throwStmt(expr: ExpressionCstNode) = onStatementComposed(ThrowCstNode(parent, tokenStart, tokenEnd, expr))

  fun block(block: StatementScope.() -> Unit) = BlockCstNode(
    BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).statements,
    parent, tokenStart, tokenEnd
  )

  fun forInStmt(varType: TypeCstNode, varName: String, isVarNullable: Boolean, inNode: ExpressionCstNode, block: StatementScope.() -> Unit) = ForInCstNode(
    varType, varName, isVarNullable, inNode, BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).asBlock(),
    parent, tokenStart, tokenEnd
  )

  fun forMultiVarInStmt(declarations: List<Triple<TypeCstNode, String, Boolean>>, inNode: ExpressionCstNode, block: StatementScope.() -> Unit) =
    ForInMultiVarCstNode(
      declarations, inNode, BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).asBlock(),
    parent, tokenStart, tokenEnd
  )

  fun forVarStmt(
    varType: TypeCstNode, varName: String, isVarNullable: Boolean,
    initExpr: ExpressionCstNode,
    conditionExpr: ExpressionCstNode,
    iteratorExpr: ExpressionCstNode,
    block: StatementScope.() -> Unit) =
    ForVarCstNode(
      VariableDeclarationCstNode(varType, identifierToken(varName), initExpr, isVarNullable, parent, tokenStart, tokenEnd),
      conditionExpr, ExpressionStatementCstNode(iteratorExpr),
      BlockStatementScope(tokenStart, tokenEnd, parent).apply(block).asBlock(),
      parent, tokenStart, tokenEnd
    )

  fun multiVarDeclStmt(declarations: List<Triple<TypeCstNode, String, Boolean>>, expr: ExpressionCstNode) = onStatementComposed(
    MultiVarDeclarationCstNode(parent, tokenStart, tokenEnd, declarations, expr)
  )

  fun varDeclStmt(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?, isNullable: Boolean = false) = onStatementComposed(
    VariableDeclarationCstNode(typeNode,
      LexToken.dummy(name), expr, isNullable, parent, tokenStart, tokenEnd)
  )

  fun returnStmt(expr: ExpressionCstNode? = null) = onStatementComposed(ReturnCstNode(parent = parent, expressionNode = expr, tokenStart = tokenStart, tokenEnd = tokenEnd))

  fun ifStmt(
    condition: ExpressionCstNode, compose: IfElseStatementScope.() -> Unit
  ) = onStatementComposed(IfElseStatementScope(condition).apply(compose).asIfStmt())

  fun tryCatchStmt(compose: TryCatchStatementScope.() -> Unit
  ) = onStatementComposed(TryCatchStatementScope().apply(compose).asTryCatchStmt())

  fun whileStmt(
    condition: ExpressionCstNode, compose: StatementScope.() -> Unit
  ) = onStatementComposed(
    WhileCstNode(
      parent,
      tokenStart,
      tokenEnd,
      condition,
      BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock(),
    )
  )

  fun doWhileStmt(
    condition: ExpressionCstNode, compose: StatementScope.() -> Unit
  ) = onStatementComposed(
    DoWhileStatementCstNode(
      parent,
      tokenStart,
      tokenEnd,
      BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock(),
      condition,
    )
  )

  protected open fun <T: StatementCstNode> onStatementComposed(statement: T): T = statement

}

/**
 * BlockStatementScope is a StatementScope that collects composed statements to create the block
 */
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

  fun trueBlock(compose: StatementScope.() -> Unit) {
    trueStatement = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  fun falseBlock(compose: StatementScope.() -> Unit) {
    falseStatement = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  internal fun asIfStmt(): IfStatementCstNode {
    val trueStatement = this.trueStatement ?: throw IllegalStateException("true statement must be set")
    return IfStatementCstNode(condition, trueStatement, falseStatement, parent, tokenStart, tokenEnd)
  }
}

class TryCatchStatementScope(
  private val tokenStart: LexToken = LexToken.DUMMY,
  private val tokenEnd: LexToken = LexToken.DUMMY,
  private val parent: CstNode? = null
) {

  // put here instead of a field because the outer scope might be a BlockScope that records statements
  private val resources = mutableListOf<VariableDeclarationCstNode>()
  private val catchNodes = mutableListOf<Triple<List<TypeCstNode>, String, StatementCstNode>>()
  private var finallyNode: StatementCstNode? = null
  private var tryStmt: StatementCstNode? = null

  fun resource(type: TypeCstNode, isNullable: Boolean, name: String, expr: ExpressionCstNode) {
    resources.add(VariableDeclarationCstNode(type, identifierToken(name), expr, isNullable, parent, tokenStart, tokenEnd))
  }

  fun tryStmt(compose: StatementScope.() -> StatementCstNode) {
    tryStmt = compose.invoke(StatementScope(tokenStart, tokenEnd, parent))
  }
  fun tryBlock(compose: StatementScope.() -> Unit) {
    tryStmt = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  fun finallyStmt(compose: StatementScope.() -> StatementCstNode) {
    finallyNode = compose.invoke(StatementScope(tokenStart, tokenEnd, parent))
  }

  fun finallyBlock(compose: StatementScope.() -> Unit) {
    finallyNode = BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()
  }

  fun catchStmt(types: List<TypeCstNode>, varName: String, compose: StatementScope.() -> StatementCstNode) {
    catchNodes.add(Triple(types, varName, compose.invoke(StatementScope(tokenStart, tokenEnd, parent))))
  }
  fun catchBlock(types: List<TypeCstNode>, varName: String, compose: StatementScope.() -> Unit) {
    catchNodes.add(Triple(types, varName, BlockStatementScope(tokenStart, tokenEnd, parent).apply(compose).asBlock()))
  }

  internal fun asTryCatchStmt(): TryCatchCstNode {
    val tryStmt = this.tryStmt ?: throw IllegalStateException("try statement must be set")
    return TryCatchCstNode(parent, tokenStart, tokenEnd, tryStmt, resources, catchNodes, finallyNode)
  }
}