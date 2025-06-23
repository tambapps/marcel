package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaArrayType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class NewArrayNode(
  type: JavaArrayType,
  val sizeExpr: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(type, tokenStart, tokenEnd) {

  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  constructor(type: JavaArrayType, sizeExpr: ExpressionNode, token: LexToken): this(type, sizeExpr, token, token)

  override val type: JavaArrayType
    get() = super.type as JavaArrayType

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}