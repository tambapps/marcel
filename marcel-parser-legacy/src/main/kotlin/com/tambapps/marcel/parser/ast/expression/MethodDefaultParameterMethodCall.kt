package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class MethodDefaultParameterMethodCall(val ownerType: JavaType, val methodName: String, val expectedType: JavaType) : AbstractExpressionNode(LexToken.dummy()) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}