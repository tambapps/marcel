package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

class MarcelArrayLengthField(javaType: JavaType, name: String) : MarcelField(name) {
  init {
    addGetter(JavaArrayLengthField(javaType, name))
  }

  private class JavaArrayLengthField(override val owner: JavaType, override val name: String): JavaField {
    override val type = JavaType.int
    override val visibility = Visibility.PUBLIC
    override val isGettable = true
    override val isSettable = false
    override val isStatic = true
    override val isFinal = true
    override fun isAccessibleFrom(javaType: JavaType) = true
  }
}
