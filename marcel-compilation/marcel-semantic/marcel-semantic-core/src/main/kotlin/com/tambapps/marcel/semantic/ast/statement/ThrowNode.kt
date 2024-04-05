package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class ThrowNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val expressionNode: ExpressionNode
) : AbstractStatementNode(tokenStart, tokenEnd) {

  constructor(
    node: CstNode,
    expressionNode: ExpressionNode
  ) : this(node.tokenStart, node.tokenEnd, expressionNode)

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}