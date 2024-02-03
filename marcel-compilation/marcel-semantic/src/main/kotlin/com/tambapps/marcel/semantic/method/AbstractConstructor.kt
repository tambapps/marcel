package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.type.JavaType

abstract class AbstractConstructor(
  override val ownerClass: JavaType,
  override val parameters: List<MethodParameter>
) : AbstractMethod() {
  override val name: String = JavaMethod.CONSTRUCTOR_NAME
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val actualReturnType get() = returnType
  override val isConstructor = true
  override val isDefault = false
  override val isAbstract = false
  override val isStatic = false
  override val isAsync = false


  override fun toString(): String {
    return "${ownerClass.className}(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") "
  }
}
