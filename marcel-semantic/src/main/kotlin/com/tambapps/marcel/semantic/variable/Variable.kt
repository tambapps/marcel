package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

/**
 * A Marcel Variable. It can be a local variable, a getter/setter, a field
 */
interface Variable : JavaTyped {

  fun <T> accept(visitor: VariableVisitor<T>): T

  // TODO move this to MarcelField and add check or set enum/boolean as a parameter
  fun isAccessibleFrom(javaType: JavaType): Boolean

  override val type: JavaType
  val name: String
  val isFinal: Boolean

  val isGettable: Boolean
  val isSettable: Boolean
}