package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class ReturnCstNode(parent: CstNode? = null, val expressionNode: ExpressionCstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    return if (expressionNode != null) "return $expressionNode;" else "return;"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReturnCstNode) return false
    if (!super.equals(other)) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + expressionNode.hashCode()
    return result
  }
}