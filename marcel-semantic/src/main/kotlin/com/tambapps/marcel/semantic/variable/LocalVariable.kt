package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.type.JavaType

/**
 * A local variable in a method
 */
class LocalVariable constructor(override var type: JavaType, override var name: String,
                                internal val nbSlots: Int,
                                val index: Int = 0,
                                override var isFinal: Boolean): AbstractVariable() {

  override val isGettable = true
  override val isSettable = true

  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

  fun reset(type: JavaType, name: String, isSettable: Boolean) {
    this.type = type
    this.name = name
    this.isFinal = isSettable
  }

  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access) = true
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LocalVariable

    return index == other.index
  }

  override fun hashCode(): Int {
    return index
  }
}
