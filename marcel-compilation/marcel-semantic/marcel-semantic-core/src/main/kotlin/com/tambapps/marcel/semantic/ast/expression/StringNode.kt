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

  constructor(parts: List<ExpressionNode>, node: CstNode) : this(
    parts,
    node.tokenStart,
    node.tokenEnd
  )

  constructor(parts: List<ExpressionNode>): this(parts, parts.first().tokenStart, parts.last().tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

  override fun toString() = parts.joinToString(separator = " + ")
}