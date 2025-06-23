package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.symbol.Symbol
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

open class MethodParameter constructor(override val type: JavaType,
                                       val rawType: JavaType,
                                       override val nullness: Nullness,
                                       override val name: String,
                                       override val isFinal: Boolean = false,
                                       val isSynthetic: Boolean = false,
                                       val annotations: MutableList<AnnotationNode>,
                                       val defaultValue: ExpressionNode? = null): Symbol {
  constructor(type: JavaType, nullness: Nullness, name: String, annotations: List<AnnotationNode>, defaultValue: ExpressionNode?, isFinal: Boolean = false, isSynthetic: Boolean = false):
      this(type, type, nullness, name, isFinal, isSynthetic, annotations.toMutableList(), defaultValue)
  constructor(type: JavaType, nullness: Nullness, name: String, isFinal: Boolean = false, isSynthetic: Boolean = false):
      this(type, nullness, name, emptyList(), null, isFinal, isSynthetic)
  constructor(type: JavaType, nullness: Nullness, name: String, defaultValue: ExpressionNode?): this(type, nullness, name, emptyList(), defaultValue)

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