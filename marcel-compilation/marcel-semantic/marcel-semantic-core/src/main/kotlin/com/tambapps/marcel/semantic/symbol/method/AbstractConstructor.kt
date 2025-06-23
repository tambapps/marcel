package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

abstract class AbstractConstructor(
  override val ownerClass: JavaType,
  override val parameters: List<MethodParameter>
) : AbstractMethod() {
  override val name: String = MarcelMethod.CONSTRUCTOR_NAME
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val isConstructor = true
  override val isDefault = false
  override val isAbstract = false
  override val isStatic = false
  override val isAsync = false
  override val nullness: Nullness
    get() = Nullness.NOT_NULL

}
