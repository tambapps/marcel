package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class VoidExpressionNode(tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionNode(JavaType.void, tokenStart, tokenEnd) {

  constructor(token: LexToken): this(token, token)
  constructor(node: AstNode): this(node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = "<void>"
}