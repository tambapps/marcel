package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

class VoidExpressionNode(token: LexToken) : AbstractExpressionNode(JavaType.void, token) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}