package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class ToStringNode constructor(token: LexToken, val expressionNode: ExpressionNode): AbstractExpressionNode(token) {

  companion object {
    fun of(expressionNode: ExpressionNode): ToStringNode {
      return of(expressionNode.token, expressionNode)
    }

    fun of(token: LexToken, expressionNode: ExpressionNode): ToStringNode {
      return if (expressionNode is ToStringNode) expressionNode else ToStringNode(token, expressionNode)
    }
  }

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}