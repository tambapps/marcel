package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

open class WhenCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val branches: MutableList<Pair<ExpressionCstNode, StatementCstNode>>,
  val elseStatement: StatementCstNode?
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WhenCstNode) return false

    if (branches != other.branches) return false
    if (elseStatement != other.elseStatement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = branches.hashCode()
    result = 31 * result + (elseStatement?.hashCode() ?: 0)
    return result
  }
}