package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.AbstractVariable
import com.tambapps.marcel.semantic.variable.Variable

sealed class AbstractField: AbstractVariable(), MarcelField {

  override fun toString(): String {
    return "$type $name"
  }

  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access): Boolean {
    return visibility.canAccess(javaType, owner)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AbstractField) return false
    if (!super.equals(other)) return false
    if (owner != other.owner) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + owner.hashCode()
    return result
  }
}
