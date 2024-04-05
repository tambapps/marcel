package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.type.JavaType

class InstanceOfNode(
  val instanceType: JavaType,
  val expressionNode: ExpressionNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(JavaType.boolean, tokenStart, tokenEnd) {

  constructor(
    instanceType: JavaType,
    expressionNode: ExpressionNode,
    node: CstNode
  ) : this(instanceType, expressionNode, node.tokenStart, node.tokenEnd)

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)
}