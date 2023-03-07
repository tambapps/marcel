package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType

class MethodParameterNode constructor(val token: LexToken, type: JavaType, rawType: JavaType, name: String, isFinal: Boolean,
                                      defaultValue: ExpressionNode?) :
  MethodParameter(type, rawType, name, isFinal, defaultValue) {
  constructor(token: LexToken, type: JavaType, name: String, isFinal: Boolean = false, defaultValue: ExpressionNode?): this(token, type, type, name, isFinal, defaultValue)
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(LexToken.dummy(), type, name, isFinal, null)
  constructor(token: LexToken, type: JavaType, name: String, defaultValue: ExpressionNode?): this(token, type, name, false, defaultValue)
  constructor(parameter: MethodParameter): this(LexToken.dummy(), parameter.type, parameter.rawType, parameter.name, parameter.isFinal, null)

}