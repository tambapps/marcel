package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor

/**
 * Field from a Script's Binding
 */
class BoundField constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType
) : AbstractField() {

  override val nullness: Nullness
    get() = Nullness.UNKNOWN
  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  override val isGettable = true
  override val isSettable = true
    fun withOwner(owner: JavaType) = BoundField(type, name, owner)
  override val visibility = Visibility.PUBLIC
  override val isStatic = false
  override val isFinal = false

}