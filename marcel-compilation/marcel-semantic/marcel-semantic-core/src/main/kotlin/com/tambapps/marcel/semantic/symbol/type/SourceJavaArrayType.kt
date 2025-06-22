package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation

/**
 * A java type representing an array of a class that is NOT loaded on the classpath
 */
class SourceJavaArrayType internal constructor(
  override val elementsType: JavaType
): SourceJavaType(visibility = Visibility.PUBLIC,
  className = "[L${elementsType.className};",
  genericTypes = emptyList(), superType = JavaType.Object,
  isInterface = false, isFunctionalInterface = false,
  directlyImplementedInterfaces = mutableSetOf(),
  isScript = false, isEnum = false, isExtensionType = false, isAbstract = false,
  isAnnotation = false,
  isFinal = false,
  globalExtendedType = null), JavaArrayType {

  override val isFinal = true
  override val asArrayType: JavaArrayType get() = this
  override val isArray = true

  override fun addImplementedInterface(javaType: JavaType) {
    throw UnsupportedOperationException()
  }

  override fun addAnnotation(annotation: JavaAnnotation) {
    throw UnsupportedOperationException()
  }

  override fun toString(): String {
    return "$elementsType[]"
  }
}
