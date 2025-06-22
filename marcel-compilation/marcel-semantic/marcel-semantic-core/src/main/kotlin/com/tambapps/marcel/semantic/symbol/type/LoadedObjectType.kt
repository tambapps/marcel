package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

/**
 * [JavaType] of an object class available on the classpath
 */
open class LoadedObjectType(
  realClazz: Class<*>,
  genericTypes: List<JavaType>,
  override val nullness: Nullness = Nullness.NOT_NULL
): LoadedJavaType(realClazz, genericTypes) {

  override val packageName: String? = realClazz.`package`?.name
  override val visibility = Visibility.fromAccess(realClazz.modifiers)
  override val isScript = false

  constructor(realClazz: Class<*>): this(realClazz, emptyList())

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes == this.genericTypes) return this
    // primitive collection
    if ((this == JavaType.List || this == JavaType.Set) && genericTypes.size == 1 && genericTypes.first().primitive) {
      val map = if (this == JavaType.List) JavaType.PRIMITIVE_LIST_MAP else JavaType.PRIMITIVE_SET_MAP
      val primitiveType = genericTypes.first()
      if (map[primitiveType] != null) {
        return map[primitiveType]!!
      }
    }
    if (genericTypes.any { it.primitive }) throw MarcelSemanticException(LexToken.DUMMY, "Cannot have a primitive generic type")
    if (isLambda && genericTypes.size == realClazz.typeParameters.size - 1) {
      return LoadedObjectType(realClazz, genericTypes + JavaType.Object)
    }

    if (genericTypes.size != realClazz.typeParameters.size
      // for lambda, we can omit return type. It will be cast
      && !isLambda && genericTypes.size != realClazz.typeParameters.size - 1) throw MarcelSemanticException(LexToken.DUMMY, "Typed $realClazz expects ${realClazz.typeParameters.size} parameters")
    return LoadedObjectType(realClazz, genericTypes)
  }

  override fun raw(): JavaType {
    return LoadedObjectType(realClazz, emptyList())
  }
}
