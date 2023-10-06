package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.Variable

sealed interface JavaField: Variable {
  val owner: JavaType
  val visibility: Visibility
  val isStatic: Boolean

  val isGettable: Boolean
  val isSettable: Boolean
}
