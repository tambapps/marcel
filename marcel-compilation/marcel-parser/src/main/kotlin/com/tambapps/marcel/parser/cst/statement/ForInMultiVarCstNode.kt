package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ForInMultiVarCstNode(
  val declarations: List<Triple<TypeCstNode, String, Boolean>>,
  val inNode: ExpressionCstNode,
  val statementNode: StatementCstNode,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ForInMultiVarCstNode) return false

    if (declarations != other.declarations) return false
    if (inNode != other.inNode) return false
    if (statementNode != other.statementNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = declarations.hashCode()
    result = 31 * result + inNode.hashCode()
    result = 31 * result + statementNode.hashCode()
    return result
  }
}