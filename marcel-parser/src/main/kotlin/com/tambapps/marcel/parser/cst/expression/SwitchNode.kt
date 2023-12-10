package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationNode

class SwitchNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  branches: MutableList<Pair<ExpressionNode, StatementNode>>,
  elseStatement: StatementNode?,
  // expression should be taken from switch expression
  val varDeclaration: VariableDeclarationNode?,
  val switchExpression: ExpressionNode
) : WhenNode(parent, tokenStart, tokenEnd, branches, elseStatement) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}