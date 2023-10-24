package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class ArrayAccessNode(
  val owner: ExpressionNode,
  val indexNode: ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionNode(owner.type.asArrayType.elementsType, tokenStart, tokenEnd) {
  constructor(owner: ExpressionNode, indexNode: ExpressionNode, node: CstNode)
      : this(owner, indexNode, node.tokenStart, node.tokenEnd)

  val arrayType = owner.type.asArrayType
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}