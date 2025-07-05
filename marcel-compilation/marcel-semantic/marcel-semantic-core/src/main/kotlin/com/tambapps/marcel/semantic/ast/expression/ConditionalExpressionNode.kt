package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class ConditionalExpressionNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val condition: ExpressionNode,
  val trueExpression: ExpressionNode,
  var falseExpression: ExpressionNode?,
) :
  AbstractExpressionNode(tokenStart, tokenEnd) {

  constructor(condition: ExpressionNode, trueExpression: ExpressionNode, falseExpression: ExpressionNode?): this(condition.tokenStart, trueExpression.tokenEnd, condition, trueExpression, falseExpression)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

  override val type: JavaType
    get() = if (falseExpression != null) JavaType.commonType(trueExpression.type, falseExpression!!.type) else trueExpression.type
  override val nullness: Nullness
    get() = if (falseExpression != null) Nullness.of(trueExpression.nullness, falseExpression!!.nullness) else Nullness.NULLABLE

}