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
}
