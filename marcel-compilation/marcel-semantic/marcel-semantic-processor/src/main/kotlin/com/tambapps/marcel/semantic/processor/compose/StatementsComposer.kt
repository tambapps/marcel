package com.tambapps.marcel.semantic.processor.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.processor.cast.ExpressionCaster
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.scope.Scope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable
import java.util.*

/**
 * Class allowing to easily compose statements of a method
 */
class StatementsComposer constructor(
  scopeQueue: LinkedList<Scope>,
  caster: ExpressionCaster,
  nullSafetyMode: NullSafetyMode,
  symbolResolver: MarcelSymbolResolver,
  val statements: MutableList<StatementNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken,
): ExpressionComposer(scopeQueue, caster, nullSafetyMode, symbolResolver, tokenStart, tokenEnd) {

  fun addAllStmt(statements: List<StatementNode>) = this.statements.addAll(statements)

  fun addStmt(statement: StatementNode) = this.statements.add(statement)

  fun stmt(statement: StatementNode) {
    statements.add(statement)
  }

  fun stmt(expr: ExpressionNode) {
    val statement = ExpressionStatementNode(expr)
    statements.add(statement)
  }

  fun varAssignStmt(
    variable: Variable,
    expr: ExpressionNode,
    owner: ExpressionNode? = null
  ) = stmt(varAssignExpr(variable, expr, owner))

  fun returnVoidStmt() = returnStmt(VoidExpressionNode(tokenStart, tokenEnd))

  fun returnStmt(
    expr: ExpressionNode? = null,
  ) {
    val statement =
      if (expr != null) ReturnStatementNode(cast(currentMethodScope.method.returnType, expr))
      else ReturnStatementNode(null, tokenStart, tokenEnd)
    statements.add(statement)
  }

  fun ifStmt(
    condition: ExpressionNode,
    trueStmt: StatementNode,
    falseStmt: StatementNode? = null,
    add: Boolean = true
  ): IfStatementNode {
    val statement = IfStatementNode(truthyCast(condition), trueStmt, falseStmt, tokenStart, tokenEnd)
    if (add) statements.add(statement)
    return statement
  }

  inline fun ifStmt(
    condition: ExpressionNode, trueStatementsComposerFunc: StatementsComposer.() -> Unit
  ): IfStatementNode {
    val trueStatementBlock = useInnerScope {
      val trueStatementsComposer = StatementsComposer(scopeQueue, caster, nullSafetyMode, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
      trueStatementsComposerFunc.invoke(trueStatementsComposer)
      trueStatementsComposer.asBlockStatement()
    }
    val statement = IfStatementNode(
      truthyCast(condition),
      trueStatementBlock, null,
      tokenStart, tokenEnd
    )

    statements.add(statement)
    return statement
  }

  inline fun forInIteratorNodeStmt(forVariable: LocalVariable, inNode: ExpressionNode,
                                   forStatementsComposerFunc: StatementsComposer.(MethodScope, LocalVariable) -> Unit
  ) {
    useInnerScope { forScope ->
      val forStmt = forInIteratorNode(tokenStart, tokenEnd, forScope, forVariable, inNode) {
        val forStatementsComposer = StatementsComposer(scopeQueue, caster, nullSafetyMode, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
        forStatementsComposerFunc.invoke(forStatementsComposer, forScope, forVariable)
        forStatementsComposer.asBlockStatement()
      }
      statements.add(forStmt)
    }
  }
  inline fun forInArrayStmt(array: ExpressionNode, forStatementsComposerFunc: StatementsComposer.(MethodScope, LocalVariable) -> Unit) {
    useInnerScope { forScope ->
      val iVar = forScope.addLocalVariable(JavaType.int, Nullness.NOT_NULL, token = tokenStart)
      val forVariable = forScope.addLocalVariable(array.type.asArrayType.elementsType, Nullness.NOT_NULL, token = tokenStart)
      val forStmt = forInArrayNode(tokenStart, tokenEnd, forScope = forScope, inNode = array, iVar = iVar, forVariable = forVariable) {
        val forStatementsComposer = StatementsComposer(scopeQueue, this, nullSafetyMode, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
        forStatementsComposerFunc.invoke(forStatementsComposer, forScope, forVariable)
        forStatementsComposer.asBlockStatement()
      }
      statements.add(forStmt)
    }
  }
  fun throwStmt(expr: ExpressionNode) = stmt(ThrowNode(tokenStart, tokenEnd, expr))

  fun asBlockStatement() = BlockStatementNode(statements, tokenStart, tokenEnd)
}
