package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

open class WhenCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  val elseStatement: StatementCstNode?
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  var variableDeclarationNode: VariableDeclarationCstNode? = null
  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)
}