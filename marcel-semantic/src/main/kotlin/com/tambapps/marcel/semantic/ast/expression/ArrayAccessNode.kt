package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor

class ArrayAccessNode(
  val owner: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken) :
  AbstractExpressionNode(owner.type.asArrayType.elementsType, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: AstNodeVisitor<T>): T {
    TODO("Not yet implemented")
  }
}