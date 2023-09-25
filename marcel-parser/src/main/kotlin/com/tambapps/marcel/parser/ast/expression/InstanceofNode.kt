package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class InstanceofNode(token: LexToken = LexToken.dummy(), val expressionNode: ExpressionNode, val type: JavaType): AbstractExpressionNode(token) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)



}