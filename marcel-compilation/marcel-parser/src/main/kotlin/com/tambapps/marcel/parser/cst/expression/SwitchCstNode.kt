package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class SwitchCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  elseStatement: StatementCstNode?,
  // expression should be taken from switch expression
  val varDeclaration: VariableDeclarationCstNode?,
  val switchExpression: ExpressionCstNode
) : WhenCstNode(parent, tokenStart, tokenEnd, branches, elseStatement) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SwitchCstNode) return false
    if (!super.equals(other)) return false

    if (varDeclaration != other.varDeclaration) return false
    if (switchExpression != other.switchExpression) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (varDeclaration?.hashCode() ?: 0)
    result = 31 * result + switchExpression.hashCode()
    return result
  }
}