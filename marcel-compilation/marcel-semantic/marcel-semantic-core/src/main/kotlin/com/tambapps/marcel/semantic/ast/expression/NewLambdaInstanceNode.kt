package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class NewLambdaInstanceNode(
  type: JavaType,
  javaMethod: MarcelMethod,
  arguments: MutableList<ExpressionNode>,
  val lambdaNode: LambdaClassNode,
  token: LexToken
) :
  NewInstanceNode(type, javaMethod, arguments, token) {
  override val nullness: Nullness
    get() = Nullness.NOT_NULL
  }