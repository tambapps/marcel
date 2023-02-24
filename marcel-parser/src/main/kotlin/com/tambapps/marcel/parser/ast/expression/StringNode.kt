package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class StringNode(token: LexToken, val parts: List<ExpressionNode>): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return parts.joinToString(separator = " + ")
  }

}