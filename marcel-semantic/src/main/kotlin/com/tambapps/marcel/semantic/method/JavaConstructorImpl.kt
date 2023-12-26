package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Java Constructor impl useful to register to a TypeResolver
 */
class JavaConstructorImpl(
  override val visibility: Visibility,
  override val isVarArgs: Boolean,
  ownerClass: JavaType, parameters: List<MethodParameter>
) : AbstractConstructor(ownerClass, parameters)