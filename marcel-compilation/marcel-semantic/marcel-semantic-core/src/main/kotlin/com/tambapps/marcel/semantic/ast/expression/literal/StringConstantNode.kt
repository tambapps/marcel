package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType

class StringConstantNode(
  override val value: String,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(JavaType.String, tokenStart, tokenEnd),
  JavaConstantExpression {

  constructor(value: String, node: CstNode) : this(value, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString(): String {
    return "\"$value\""
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StringConstantNode

    return value == other.value
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}
