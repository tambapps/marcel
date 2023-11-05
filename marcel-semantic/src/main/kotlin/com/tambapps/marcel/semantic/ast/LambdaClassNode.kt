package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaType

class LambdaClassNode(type: JavaType, tokenStart: LexToken, tokenEnd: LexToken) : ClassNode(type, Visibility.PRIVATE, tokenStart, tokenEnd) {

  var interfaceType: JavaType? = null

  var expectedReturnType: JavaType? = null

  lateinit var constructorCallNode: ExpressionNode
  lateinit var body: StatementNode
}