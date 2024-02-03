package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class ArrayAccessNode(
  val owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val indexNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  tokenStart: LexToken, tokenEnd: LexToken) :
  com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(owner.type.asArrayType.elementsType, tokenStart, tokenEnd) {
  constructor(owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, indexNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, node: CstNode)
      : this(owner, indexNode, node.tokenStart, node.tokenEnd)

  val arrayType = owner.type.asArrayType
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}