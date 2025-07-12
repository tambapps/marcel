package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class MultiVarDeclarationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  // nullable elements because we might want to skip some
  val declarations: List<Triple<TypeCstNode, String, Boolean>?>,
  val expressionNode: ExpressionCstNode
) : AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MultiVarDeclarationCstNode) return false

    if (declarations != other.declarations) return false
    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = declarations.hashCode()
    result = 31 * result + expressionNode.hashCode()
    return result
  }
}