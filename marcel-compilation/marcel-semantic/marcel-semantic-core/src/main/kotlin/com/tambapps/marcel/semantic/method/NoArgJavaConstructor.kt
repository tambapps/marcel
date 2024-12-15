package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

class NoArgJavaConstructor(ownerClass: JavaType, override val isSynthetic: Boolean = false) :
  AbstractConstructor(ownerClass, emptyList()) {
  override val visibility = Visibility.PUBLIC
  override val isVarArgs = false

}