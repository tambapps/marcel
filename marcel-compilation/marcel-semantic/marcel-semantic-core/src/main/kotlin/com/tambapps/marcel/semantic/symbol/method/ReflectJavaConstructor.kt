package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import java.lang.reflect.Constructor

class ReflectJavaConstructor(constructor: Constructor<*>): AbstractConstructor(
  JavaType.of(constructor.declaringClass),
  constructor.parameters.map {
    ReflectJavaMethod.methodParameter(
      constructor.name,
      JavaType.of(constructor.declaringClass),
      null,
      it
    )
  }
) {
  override val visibility = Visibility.fromAccess(constructor.modifiers)
  override val isVarArgs = constructor.isVarArgs
  override val isSynthetic = constructor.isSynthetic

}
