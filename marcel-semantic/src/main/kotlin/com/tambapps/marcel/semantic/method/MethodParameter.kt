package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

open class MethodParameter constructor(override val type: JavaType, val rawType: JavaType, val name: String,
                                       val isFinal: Boolean = false,
                                       val annotations: List<AnnotationNode>,
                                       val defaultValue: ExpressionNode? = null): JavaTyped {
  constructor(type: JavaType, name: String, annotations: List<AnnotationNode>, defaultValue: ExpressionNode?, isFinal: Boolean = false): this(type, type, name, isFinal, annotations, defaultValue)
  constructor(type: JavaType, name: String, annotations: List<AnnotationNode>, isFinal: Boolean = false): this(type, name, annotations, null, isFinal)
  constructor(type: JavaType, name: String, isFinal: Boolean = false): this(type, name, emptyList(), isFinal)

  val hasDefaultValue get() = defaultValue != null
  override fun toString(): String {
    val s = "$type $name"
    return if (defaultValue != null) "$s = $defaultValue" else s
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as MethodParameter

    if (type != other.type) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }
}