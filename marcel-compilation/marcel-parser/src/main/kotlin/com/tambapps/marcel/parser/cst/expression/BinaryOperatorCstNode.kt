package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class BinaryOperatorCstNode(
  val tokenType: TokenType,
  var leftOperand: ExpressionCstNode,
  var rightOperand: ExpressionCstNode,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString(): String {
    return if (tokenType == TokenType.DOT) "$leftOperand.$tokenType.$rightOperand"
    else "$leftOperand $tokenType $rightOperand"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BinaryOperatorCstNode) return false

    if (tokenType != other.tokenType) return false
    if (leftOperand != other.leftOperand) return false
    if (rightOperand != other.rightOperand) return false

    return true
  }

  override fun hashCode(): Int {
    var result = tokenType.hashCode()
    result = 31 * result + leftOperand.hashCode()
    result = 31 * result + rightOperand.hashCode()
    return result
  }


}