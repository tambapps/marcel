package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Java Method impl useful to register to a TypeResolver
 */
class MarcelMethodImpl constructor(
  override val ownerClass: JavaType,
  override val visibility: Visibility,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val isDefault: Boolean = false,
  override val isAbstract: Boolean = false,
  override val isStatic: Boolean = false,
  override val isConstructor: Boolean = false,
  override val isVarArgs: Boolean = false,
  override val isAsync: Boolean = false,
  override val isSynthetic: Boolean = false,
  override val asyncReturnType: JavaType? = null
) : AbstractMethod() {

}