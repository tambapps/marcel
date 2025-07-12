package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class IfStatementCstNode(
  val condition: ExpressionCstNode, val trueStatementNode: StatementCstNode,
  var falseStatementNode: StatementCstNode?,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is IfStatementCstNode) return false

    if (condition != other.condition) return false
    if (trueStatementNode != other.trueStatementNode) return false
    if (falseStatementNode != other.falseStatementNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = condition.hashCode()
    result = 31 * result + trueStatementNode.hashCode()
    result = 31 * result + (falseStatementNode?.hashCode() ?: 0)
    return result
  }

}