package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType

class NoArgJavaConstructor(ownerClass: JavaType, override val isSynthetic: Boolean = false) :
  AbstractConstructor(ownerClass, emptyList()) {
  override val visibility = Visibility.PUBLIC
  override val isVarArgs = false

}