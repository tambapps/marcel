package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType

abstract class ClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType): AbstractField() {

  override val isSettable = true
  override val isGettable = true

}
