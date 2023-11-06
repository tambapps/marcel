package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaType

class LambdaClassNode(
  type: JavaType,
  val constructorNode: MethodNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : ClassNode(type, Visibility.PRIVATE, tokenStart, tokenEnd) {

  val interfaceTypes = mutableSetOf<JavaType>()

  var expectedReturnType: JavaType? = null
  val constructorParameters = mutableListOf<ExpressionNode>()

  lateinit var constructorCallNode: NewInstanceNode
  lateinit var body: StatementNode

  init {
    methods.add(constructorNode)
  }
}