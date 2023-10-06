package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.type.JavaType

/**
 * A local variable in a method
 */
class LocalVariable constructor(override var type: JavaType, override var name: String,
                                internal val nbSlots: Int,
                                val index: Int = 0,
                                override var isFinal: Boolean): AbstractVariable() {
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

  fun reset(type: JavaType, name: String, isSettable: Boolean) {
    this.type = type
    this.name = name
    this.isFinal = isSettable
  }

  override fun isAccessibleFrom(javaType: JavaType) = true
}
