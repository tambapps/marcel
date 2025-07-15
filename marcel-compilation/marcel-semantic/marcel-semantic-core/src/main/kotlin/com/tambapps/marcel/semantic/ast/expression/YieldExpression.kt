package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * Yield expression, allowing to push a value on the stack. Useful for when expressions
 */
class YieldExpression(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  /**
   * The statements to execute before pushing the value
   */
  val statement: StatementNode?,
  /**
   * The value to push
   */
  val expression: ExpressionNode
) :
  AbstractExpressionNode(tokenStart, tokenEnd) {

  override val type = expression.type


  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  override val nullness: Nullness
    get() = expression.nullness

    constructor(statement: StatementNode?, expression: ExpressionNode)
        : this(statement?.tokenStart ?: expression.tokenStart, expression.tokenEnd, statement, expression)
}