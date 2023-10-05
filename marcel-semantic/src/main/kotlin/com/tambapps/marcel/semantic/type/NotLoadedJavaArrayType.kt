package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.Visibility

class NotLoadedJavaArrayType internal  constructor(
  override val elementsType: JavaType
): NotLoadedJavaType(visibility = Visibility.PUBLIC,
  className = "[L${elementsType.className};",
  genericTypes = emptyList(), superType = JavaType.Object, isInterface = false, directlyImplementedInterfaces = mutableSetOf()), JavaArrayType {

  override val isFinal = true
  override val asArrayType: JavaArrayType get() = this

}
