package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.Variable

sealed interface MarcelField: Variable {
  val owner: JavaType
  val visibility: Visibility
  val isStatic: Boolean
  val isExtension: Boolean get() = false

  // extension field/methods are not considered static in marcel
  val isMarcelStatic: Boolean get() = isStatic && !isExtension

}
