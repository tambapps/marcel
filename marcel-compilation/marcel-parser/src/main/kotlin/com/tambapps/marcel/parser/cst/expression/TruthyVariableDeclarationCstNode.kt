package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TruthyVariableDeclarationCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val type: TypeCstNode,
  val identifierToken: LexToken,
  val expression: ExpressionCstNode
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override val value: String = identifierToken.value

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TruthyVariableDeclarationCstNode) return false

    if (type != other.type) return false
    if (identifierToken != other.identifierToken) return false
    if (expression != other.expression) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + identifierToken.hashCode()
    result = 31 * result + expression.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }
}