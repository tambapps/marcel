package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType

open class MethodParameter constructor(override val type: JavaType, val rawType: JavaType, val name: String,
                                       val isFinal: Boolean = false,
  val defaultValue: ExpressionNode? = null): AstTypedObject {
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(type, type, name, isFinal, null)

  override fun toString(): String {
    return "$type $name"
  }
}