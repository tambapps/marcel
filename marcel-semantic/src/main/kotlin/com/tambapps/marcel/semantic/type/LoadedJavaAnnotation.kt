package com.tambapps.marcel.semantic.type

import java.lang.annotation.ElementType

open class LoadedJavaAnnotation(final override val type: JavaType): JavaAnnotation {

  private var _targets: List<ElementType>? = null
  override val targets = computeTargets()

  // lazy because we don't want to load it while compiling
  private var _attributes: List<JavaAnnotation.Attribute>? = null
  override val attributes: List<JavaAnnotation.Attribute> get() {
    if (_attributes == null) {
      _attributes = type.realClazz.declaredMethods.map {
        JavaAnnotation.Attribute(it.name, LoadedObjectType(it.returnType), it.defaultValue)
      }
    }
    return _attributes!!
  }

  private fun computeTargets(): List<ElementType> {
    val targetAnnotation = type.realClazz.getAnnotation(java.lang.annotation.Target::class.java)
    return targetAnnotation?.value?.toList() ?: ElementType.values().toList()
  }
}
