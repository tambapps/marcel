package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

open class MethodParameter constructor(override val type: JavaType, val rawType: JavaType, val name: String,
                                       val isFinal: Boolean = false,
                                       val defaultValue: ExpressionNode? = null): JavaTyped {
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(type, type, name, isFinal, null)

  val hasDefaultValue get() = defaultValue != null
  override fun toString(): String {
    val s = "$type $name"
    return if (defaultValue != null) "$s = $defaultValue" else s
  }
}