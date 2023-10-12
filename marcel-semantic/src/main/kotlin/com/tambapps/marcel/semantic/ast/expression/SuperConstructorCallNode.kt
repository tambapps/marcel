package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

class SuperConstructorCallNode(
  val classType: JavaType,
  val javaMethod: JavaMethod,
  val arguments: List<ExpressionNode>,
  tokenStart: LexToken, tokenEnd: LexToken): AbstractExpressionNode(JavaType.void, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)

}