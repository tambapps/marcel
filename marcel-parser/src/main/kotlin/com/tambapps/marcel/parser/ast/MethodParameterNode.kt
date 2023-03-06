package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType

class MethodParameterNode constructor(type: JavaType, rawType: JavaType, name: String, isFinal: Boolean,
                                      defaultValue: ExpressionNode?) :
  MethodParameter(type, rawType, name, isFinal, defaultValue) {
  constructor(type: JavaType, name: String, isFinal: Boolean = false, defaultValue: ExpressionNode?): this(type, type, name, isFinal, defaultValue)
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(type, name, isFinal, null)
  constructor(type: JavaType, name: String, defaultValue: ExpressionNode?): this(type, name, false, defaultValue)
  constructor(parameter: MethodParameter): this(parameter.type, parameter.rawType, parameter.name, parameter.isFinal, null)

}