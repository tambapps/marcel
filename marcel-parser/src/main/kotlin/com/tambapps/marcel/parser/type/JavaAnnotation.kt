package com.tambapps.marcel.parser.type

import java.lang.annotation.ElementType
import kotlin.reflect.KClass

interface JavaAnnotation {

  companion object {
    fun of (klass: KClass<*>) = of(klass.java)

    fun of(clazz: Class<*>) = of(JavaType.of(clazz))
    fun of(javaType: JavaType) = LoadedJavaAnnotation(javaType)
  }

  data class Attribute(val name: String, val type: JavaType, val defaultValue: Any?)

  val attributes: List<Attribute>
  val type: JavaType
  val descriptor get() = type.descriptor

  val targets: List<ElementType>
}

open class LoadedJavaAnnotation(final override val type: JavaType): JavaAnnotation {

  private var _targets: List<ElementType>? = null
  // lazy because we don't want it to be resolved at parsing
  final override val targets: List<ElementType>
    get() {
      if (_targets == null) {
        val targetAnnotation = type.realClazz.getAnnotation(java.lang.annotation.Target::class.java)
        _targets = targetAnnotation?.value?.toList() ?: ElementType.values().toList()
      }
      return _targets!!
    }

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
}
