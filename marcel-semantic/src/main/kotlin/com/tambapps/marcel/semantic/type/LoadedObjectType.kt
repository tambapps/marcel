package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

open class LoadedObjectType(
  realClazz: Class<*>,
  genericTypes: List<JavaType>,
): LoadedJavaType(realClazz, genericTypes) {

  override val packageName: String? = realClazz.`package`?.name
  override val visibility = Visibility.fromAccess(realClazz.modifiers)
  override val isScript = false

  constructor(realClazz: Class<*>): this(realClazz, emptyList())

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes == this.genericTypes) return this
    if (genericTypes.any { it.primitive }) throw MarcelSemanticException("Cannot have a primitive generic type")
    if (isLambda && genericTypes.size == realClazz.typeParameters.size - 1) {
      return LoadedObjectType(realClazz, genericTypes + JavaType.Object)
    }

    if (genericTypes.size != realClazz.typeParameters.size
      // for lambda, we can omit return type. It will be cast
      && !isLambda && genericTypes.size != realClazz.typeParameters.size - 1) throw MarcelSemanticException("Typed $realClazz expects ${realClazz.typeParameters.size} parameters")
    return LoadedObjectType(realClazz, genericTypes)
  }

  override fun raw(): JavaType {
    return LoadedObjectType(realClazz, emptyList())
  }
}
