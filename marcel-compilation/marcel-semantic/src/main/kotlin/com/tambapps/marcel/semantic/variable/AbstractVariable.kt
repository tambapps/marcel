package com.tambapps.marcel.semantic.variable

abstract class AbstractVariable: Variable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AbstractVariable) return false

    if (name != other.name) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

}