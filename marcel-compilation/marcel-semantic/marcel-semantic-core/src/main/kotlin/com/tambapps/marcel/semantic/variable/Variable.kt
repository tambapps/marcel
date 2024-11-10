package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

/**
 * A Marcel Variable. It can be a local variable, a getter/setter, a field
 */
interface Variable : JavaTyped {
  enum class Access {
    GET, SET, ANY
  }
  fun <T> accept(visitor: VariableVisitor<T>): T

  fun isVisibleFrom(javaType: JavaType): Boolean = isVisibleFrom(javaType, Access.ANY)
  fun isVisibleFrom(javaType: JavaType, access: Access): Boolean

  override val type: JavaType
  val name: String
  val isFinal: Boolean

  val isGettable: Boolean
  val isSettable: Boolean
}