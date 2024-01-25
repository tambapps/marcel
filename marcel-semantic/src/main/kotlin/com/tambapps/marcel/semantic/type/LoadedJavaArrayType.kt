package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

/**
 * [JavaType] representing an array of a type loaded on the classpath
 */
class LoadedJavaArrayType internal constructor(
  realClazz: Class<*>,
  override val elementsType: JavaType,
): LoadedJavaType(realClazz, emptyList()), JavaArrayType {


  // constructor for non-primitive arrays
  constructor(realClazz: Class<*>): this(realClazz, JavaType.of(realClazz.componentType))

  override val visibility = Visibility.PUBLIC
  override val isArray = true
  override val packageName = null
  override val isScript = false

  override val asArrayType: JavaArrayType
    get() = this

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes.isNotEmpty()) throw MarcelSemanticException(LexToken.DUMMY, "Cannot have array type with generic types")
    return this
  }

  override fun toString(): String {
    return "$elementsType[]"
  }
}
