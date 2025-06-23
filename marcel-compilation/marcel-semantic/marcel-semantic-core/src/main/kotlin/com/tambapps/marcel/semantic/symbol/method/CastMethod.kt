package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class CastMethod private constructor(argType: JavaType): VirtualMarcelMethod() {

  override val nullness: Nullness
    get() = Nullness.UNKNOWN

  companion object {
    val METHODS = mutableListOf<MarcelMethod>().apply {
      add(CastMethod(JavaType.Object))
      addAll(JavaType.PRIMITIVES.map(::CastMethod))
    }.toList()
  }
  override fun <T> accept(visitor: MarcelMethodVisitor<T>) = visitor.visit(this)

  override val visibility = Visibility.PUBLIC
  override val name = "cast"
  override val parameters = listOf(MethodParameter(argType, Nullness.NULLABLE, "value"))

  // Object because we want this method to be accessible from any class and object is parent of any class
  override val ownerClass = JavaType.Object
  override val isStatic = true
  override val returnType = argType
  override val isVarArgs = false
}