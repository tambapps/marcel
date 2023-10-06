package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Field from a Script's Binding
 */
class BoundField constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType
) : AbstractField() {

  override val isGettable = true
  override val isSettable = true
    fun withOwner(owner: JavaType) = BoundField(type, name, owner)
  override val visibility = Visibility.PUBLIC
  override val isStatic = false
  override val isFinal = false

}