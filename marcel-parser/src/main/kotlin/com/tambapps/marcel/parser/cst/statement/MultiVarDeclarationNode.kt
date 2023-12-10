package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode

class MultiVarDeclarationNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  // nullable elements because we might want to skip some
  val declarations: List<Pair<TypeNode, String>?>,
  val expressionNode: ExpressionNode
) : AbstractStatementNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}