package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType

/**
 * Field from a class
 */
abstract class ClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType): AbstractField() {

  override val isSettable = true
  override val isGettable = true

}
