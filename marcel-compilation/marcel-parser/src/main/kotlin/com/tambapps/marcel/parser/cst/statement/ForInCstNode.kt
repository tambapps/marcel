package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ForInCstNode(
  val varType: TypeCstNode,
  val varName: String,
  val isVarNullable: Boolean,
  val inNode: ExpressionCstNode,
  val statementNode: StatementCstNode,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

}