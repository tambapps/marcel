package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class NotNode(
  val expressionNode: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionNode(tokenStart, tokenEnd) {

  override val type = JavaType.boolean
  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  constructor(expressionNode: ExpressionNode) : this(
    expressionNode,
    expressionNode.tokenStart,
    expressionNode.tokenEnd
  )

  constructor(
    expressionNode: ExpressionNode,
    node: CstNode
  ) : this(expressionNode, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}