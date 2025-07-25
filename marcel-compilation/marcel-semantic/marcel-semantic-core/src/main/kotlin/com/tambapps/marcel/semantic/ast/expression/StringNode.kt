package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class StringNode(
  val parts: List<ExpressionNode>,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionNode(tokenStart, tokenEnd) {

  override val type = JavaType.String

  override val nullness: Nullness
    get() = Nullness.NOT_NULL

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