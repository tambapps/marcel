package com.tambapps.marcel.semantic.type

import java.lang.annotation.ElementType

open class LoadedJavaAnnotationType(realClazz: Class<*>): LoadedObjectType(realClazz, emptyList()), JavaAnnotationType {

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
