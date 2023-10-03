package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode

class ExpressionStatementCstNode(parent: CstNode?,
                                 val expressionNode: CstExpressionNode,
                       tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), StatementCstNode {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ExpressionStatementCstNode) return false
    if (!super.equals(other)) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + expressionNode.hashCode()
    return result
  }

  override fun toString() = "$expressionNode;"
}