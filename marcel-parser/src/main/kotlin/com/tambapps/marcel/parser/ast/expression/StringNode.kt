package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class StringNode private constructor(token: LexToken, val parts: List<ExpressionNode>): AbstractExpressionNode(token) {

  companion object {
    fun of(token: LexToken, parts: List<ExpressionNode>): ExpressionNode {
      return if (parts.size == 1 && parts.first() is StringConstantNode) parts.first()
      else StringNode(token, parts)
    }
  }

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return parts.joinToString(separator = " + ")
  }

}