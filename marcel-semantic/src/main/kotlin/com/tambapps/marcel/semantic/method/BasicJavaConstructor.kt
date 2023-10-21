package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

class BasicJavaConstructor(
  override val visibility: Visibility,
  ownerClass: JavaType, parameters: List<MethodParameter>
) : AbstractConstructor(ownerClass, parameters)