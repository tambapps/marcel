package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType

/**
 * Error node used to allow continuing semantic analysis and collect further errors.
 * Such nodes are not present in a semantically valid AST
 */
class ExprErrorNode private constructor(
  type: JavaType,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionNode(type, tokenStart, tokenEnd) {

  constructor(token: LexToken, type: JavaType?) : this(type ?: JavaType.Object, token, token)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}