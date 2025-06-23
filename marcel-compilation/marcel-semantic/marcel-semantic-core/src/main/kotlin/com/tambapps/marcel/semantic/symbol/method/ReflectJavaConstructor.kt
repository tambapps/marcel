package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

class ReflectJavaConstructor(constructor: Constructor<*>): AbstractConstructor(
  JavaType.of(constructor.declaringClass),
  constructor.parameters.map {
    ReflectJavaMethod.methodParameter(
      constructor.name,
      constructor.declaringClass,
      JavaType.of(constructor.declaringClass),
      null,
      it
    )
  }
) {
  override val isFinal = (Modifier.FINAL and constructor.modifiers) != 0
  override val visibility = Visibility.fromAccess(constructor.modifiers)
  override val isVarArgs = constructor.isVarArgs
  override val isSynthetic = constructor.isSynthetic

}
