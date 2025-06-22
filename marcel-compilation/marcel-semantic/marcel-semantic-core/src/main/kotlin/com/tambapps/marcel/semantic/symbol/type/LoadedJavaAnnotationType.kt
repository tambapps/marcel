package com.tambapps.marcel.semantic.symbol.type

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

/**
 * [JavaType] representing an annotation loaded on the classpath
 */
open class LoadedJavaAnnotationType(realClazz: Class<*>): LoadedObjectType(realClazz, emptyList()), JavaAnnotationType {

  override val retentionPolicy
  = realClazz.getAnnotation(java.lang.annotation.Retention::class.java)?.value
    ?: RetentionPolicy.CLASS
  override val targets = computeTargets()

  // lazy because we don't want to load it while compiling
  private var _attributes: List<JavaAnnotationType.Attribute>? = null
  override val attributes: List<JavaAnnotationType.Attribute> get() {
    if (_attributes == null) {
      _attributes = realClazz.declaredMethods.map {
        JavaAnnotationType.Attribute(it.name, LoadedObjectType(it.returnType), it.defaultValue)
      }
    }
    return _attributes!!
  }

  private fun computeTargets(): List<ElementType> {
    val targetAnnotation = realClazz.getAnnotation(java.lang.annotation.Target::class.java)
    return targetAnnotation?.value?.toList() ?: ElementType.values().toList()
  }
}
