package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class InstanceOfNode(
  val instanceType: JavaType,
  val expressionNode: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(tokenStart, tokenEnd) {

  override val type = JavaType.boolean

  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  constructor(
    instanceType: JavaType,
    expressionNode: ExpressionNode,
    node: CstNode
  ) : this(instanceType, expressionNode, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)
}