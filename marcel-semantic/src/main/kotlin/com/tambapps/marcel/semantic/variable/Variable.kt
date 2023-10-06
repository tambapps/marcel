package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

interface Variable : JavaTyped {

  fun isAccessibleFrom(javaType: JavaType): Boolean

  override val type: JavaType
  val name: String
  val isFinal: Boolean
}