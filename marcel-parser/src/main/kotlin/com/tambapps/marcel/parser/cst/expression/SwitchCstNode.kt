package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

class SwitchCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  elseStatement: StatementCstNode?,
  // TODO document variable declaration in switch. See test_switch for an example
  // expression should be taken from switch expression
  val varDeclaration: VariableDeclarationCstNode?,
  val switchExpression: ExpressionCstNode
) : WhenCstNode(parent, tokenStart, tokenEnd, branches, elseStatement) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
}