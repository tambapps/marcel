package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.StatementNodeVisitor
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable

class ForInIteratorStatementNode constructor(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val variable: LocalVariable,
  val iteratorVariable: LocalVariable,
  val iteratorExpression: ExpressionNode,
  val nextMethodCall: ExpressionNode,
  var bodyStatement: StatementNode
) : AbstractStatementNode(tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)
}