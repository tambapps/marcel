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
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ForInCstNode) return false

    if (isVarNullable != other.isVarNullable) return false
    if (varType != other.varType) return false
    if (varName != other.varName) return false
    if (inNode != other.inNode) return false
    if (statementNode != other.statementNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = isVarNullable.hashCode()
    result = 31 * result + varType.hashCode()
    result = 31 * result + varName.hashCode()
    result = 31 * result + inNode.hashCode()
    result = 31 * result + statementNode.hashCode()
    return result
  }
}