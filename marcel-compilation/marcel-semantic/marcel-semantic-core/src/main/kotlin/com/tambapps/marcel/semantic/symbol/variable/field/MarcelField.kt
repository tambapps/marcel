package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.Variable

sealed interface MarcelField: Variable {
  val owner: JavaType
  val visibility: Visibility
  val isStatic: Boolean
  val isExtension: Boolean get() = false

  // extension field/methods are not considered static in marcel
  val isMarcelStatic: Boolean get() = isStatic && !isExtension

}
