package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class VariableDeclarationCstNode constructor(
  val type: TypeCstNode,
  val variableToken: LexToken,
  val expressionNode: ExpressionCstNode?,
  val isNullable: Boolean,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken
) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override val value: String = variableToken.value

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    val s = "${type.toString(isNullable)} $value"
    return if (expressionNode != null) "$s = $expressionNode;" else "$s;"
  }

}