package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor

class VoidExpressionNode(token: LexToken) : AbstractExpressionNode(token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}