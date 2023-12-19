package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class StringNode(
  val parts: List<ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionNode(JavaType.String, tokenStart, tokenEnd) {

  constructor(parts: List<ExpressionNode>, node: CstNode): this(parts, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}