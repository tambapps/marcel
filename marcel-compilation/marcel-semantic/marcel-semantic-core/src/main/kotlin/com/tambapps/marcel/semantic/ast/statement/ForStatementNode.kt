package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor

class ForStatementNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val initStatement: StatementNode,
  val condition: ExpressionNode,
  val iteratorStatement: StatementNode,
  var bodyStatement: StatementNode
) : AbstractStatementNode(tokenStart, tokenEnd) {

  constructor(
    node: CstNode,
    initStatement: StatementNode,
    condition: ExpressionNode,
    iteratorStatement: StatementNode,
    bodyStatement: StatementNode
  ): this(node.tokenStart, node.tokenEnd, initStatement, condition, iteratorStatement, bodyStatement)
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}