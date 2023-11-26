package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

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
    throw MarcelSemanticException(LexToken.DUMMY, "Cannot have array type with generic types")
  }

  override fun toString(): String {
    return "$elementsType[]"
  }
}
