package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.AbstractVariable

sealed class AbstractField: AbstractVariable(), JavaField {

  override fun toString(): String {
    return "$type $name"
  }

  override fun isAccessibleFrom(javaType: JavaType): Boolean {
    return visibility.canAccess(javaType, owner)
  }
}
