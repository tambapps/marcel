package com.tambapps.marcel.semantic.symbol.variable

import com.tambapps.marcel.semantic.symbol.type.JavaType

/**
 * A local variable in a method
 */
class LocalVariable constructor(override val type: JavaType, override val name: String,
                                val nbSlots: Int,
                                val index: Int = 0,
                                override val isFinal: Boolean): AbstractVariable() {

  override val isGettable = true
  override val isSettable = !isFinal

  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access) = true

  fun withIndex(index: Int) = LocalVariable(type, name, nbSlots, index, isFinal)

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
