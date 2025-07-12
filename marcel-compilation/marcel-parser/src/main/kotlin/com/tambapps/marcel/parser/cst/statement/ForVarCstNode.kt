package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ForVarCstNode(
  val varDecl: VariableDeclarationCstNode,
  val condition: ExpressionCstNode,
  val iteratorStatement: StatementCstNode,
  val bodyStatement: StatementCstNode,
  parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ForVarCstNode) return false

    if (varDecl != other.varDecl) return false
    if (condition != other.condition) return false
    if (iteratorStatement != other.iteratorStatement) return false
    if (bodyStatement != other.bodyStatement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = varDecl.hashCode()
    result = 31 * result + condition.hashCode()
    result = 31 * result + iteratorStatement.hashCode()
    result = 31 * result + bodyStatement.hashCode()
    return result
  }
}