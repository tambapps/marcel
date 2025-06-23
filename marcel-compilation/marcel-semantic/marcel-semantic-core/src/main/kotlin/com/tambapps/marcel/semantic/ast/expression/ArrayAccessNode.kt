package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.symbol.type.Nullness

class ArrayAccessNode(
  val owner: ExpressionNode,
  val indexNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken
) :
  AbstractExpressionNode(
    owner.type.asArrayType.elementsType,
    tokenStart,
    tokenEnd
  ) {
  constructor(
    owner: ExpressionNode,
    indexNode: ExpressionNode,
    node: CstNode
  )
      : this(owner, indexNode, node.tokenStart, node.tokenEnd)

  override val nullness: Nullness
    get() = if (arrayType.elementsType.primitive) Nullness.NOT_NULL else Nullness.UNKNOWN

  val arrayType = owner.type.asArrayType
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}