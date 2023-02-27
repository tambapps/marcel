package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class BooleanExpressionNode private constructor(token: LexToken, val innerExpression: ExpressionNode): AbstractExpressionNode(token) {

  companion object {
    fun of(expressionNode: ExpressionNode): BooleanExpressionNode {
      return of(expressionNode.token, expressionNode)
    }

    fun of(token: LexToken, expressionNode: ExpressionNode): BooleanExpressionNode {
      return if (expressionNode is BooleanExpressionNode) expressionNode else BooleanExpressionNode(token, expressionNode)
    }
  }
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "bool($innerExpression)"
  }
}