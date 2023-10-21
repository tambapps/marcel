package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Java Method impl useful to register to a TypeResolver
 */
class JavaMethodImpl(
  override val ownerClass: JavaType,
  override val visibility: Visibility,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val isDefault: Boolean,
  override val isAbstract: Boolean,
  override val isStatic: Boolean,
  override val isConstructor: Boolean
) : AbstractMethod() {

  override val actualReturnType = returnType
}