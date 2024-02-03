package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class StringNode(
  val parts: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.String, tokenStart, tokenEnd) {

  constructor(parts: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>, node: CstNode): this(parts, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}