package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType

class MethodParameterNode constructor(val token: LexToken, type: JavaType, rawType: JavaType, name: String, isFinal: Boolean,
                                      defaultValue: ExpressionNode?, override val annotations: List<AnnotationNode>) :
  MethodParameter(type, rawType, name, isFinal, defaultValue), Annotable {
  constructor(token: LexToken, type: JavaType, name: String, isFinal: Boolean = false, defaultValue: ExpressionNode? = null, annotations: List<AnnotationNode> = emptyList()): this(token, type, type, name, isFinal, defaultValue, annotations)
  constructor(token: LexToken, type: JavaType, name: String, isFinal: Boolean = false, defaultValue: ExpressionNode? = null): this(token, type, type, name, isFinal, defaultValue, emptyList())
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(LexToken.dummy(), type, name, isFinal, null, emptyList())
  constructor(token: LexToken, type: JavaType, name: String, defaultValue: ExpressionNode?, annotations: List<AnnotationNode>): this(token, type, name, false, defaultValue, annotations)
  constructor(token: LexToken, type: JavaType, name: String, defaultValue: ExpressionNode?): this(token, type, name, false, defaultValue, emptyList())
  constructor(parameter: MethodParameterNode): this(parameter.token, parameter)
  constructor(token: LexToken, parameter: MethodParameter): this(token, parameter.type, parameter.rawType, parameter.name, parameter.isFinal, null, emptyList())

}