package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

class CastMethod private constructor(argType: JavaType): CustomMethod() {

  companion object {
    val METHODS = mutableListOf<JavaMethod>().apply {
      add(CastMethod(JavaType.Object))
      addAll(JavaType.PRIMITIVES.map(::CastMethod))
    }.toList()
  }
  override fun <T> accept(visitor: JavaMethodVisitor<T>) = visitor.visit(this)

  override val visibility = Visibility.PUBLIC
  override val name = "cast"
  override val parameters = listOf(MethodParameter(argType, "value"))

  // Object because we want this method to be accessible from any class and object is parent of any class
  override val ownerClass = JavaType.Object
  override val isStatic = true
  override val returnType = argType
  override val actualReturnType = argType
  override val isVarArgs = false
}