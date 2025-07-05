package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class VoidExpressionNode(tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionNode(tokenStart, tokenEnd) {

  override val type = JavaType.void
  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  constructor(token: LexToken): this(token, token)
  constructor(node: AstNode): this(node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = "<void>"
}