package com.tambapps.marcel.semantic.symbol.variable

import com.tambapps.marcel.semantic.symbol.Symbol
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.JavaTyped
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * A Marcel Variable. It can be a local variable, a getter/setter, a field
 */
interface Variable : Symbol {
  enum class Access {
    GET, SET, ANY
  }
  fun <T> accept(visitor: VariableVisitor<T>): T

  fun isVisibleFrom(javaType: JavaType): Boolean = isVisibleFrom(javaType, Access.ANY)
  fun isVisibleFrom(javaType: JavaType, access: Access): Boolean

  override val type: JavaType
  override val name: String
  override val isFinal: Boolean
  override val nullness: Nullness

  val isGettable: Boolean
  val isSettable: Boolean
}