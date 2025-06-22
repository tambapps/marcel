package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import kotlin.reflect.KClass

/**
 * [JavaType] of a primitive type
 */
class JavaPrimitiveType internal constructor(
  objectKlazz: KClass<*>,
  val isNumber: Boolean,
): LoadedJavaType(objectKlazz.javaPrimitiveType!!, emptyList()) {

  override val visibility = Visibility.PUBLIC
  override val packageName = null
  override val isScript = false
  val objectClass = objectKlazz.java
  override val objectType: JavaType
    get() = JavaType.of(objectClass)
  override val nullness get() = Nullness.NOT_NULL
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaPrimitiveType {
    if (genericTypes.isNotEmpty()) throw MarcelSemanticException(LexToken.DUMMY, "Cannot have primitive type with generic types")
    return this
  }

  override val asPrimitiveType: JavaPrimitiveType
    get() = this
  override fun raw(): JavaType {
    return this
  }
}
