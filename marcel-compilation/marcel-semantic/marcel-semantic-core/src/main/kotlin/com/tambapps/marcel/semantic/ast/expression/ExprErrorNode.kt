package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

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

  override val nullness: Nullness
    get() = Nullness.UNKNOWN

  constructor(token: LexToken, type: JavaType?) : this(type ?: JavaType.Object, token, token)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)
}