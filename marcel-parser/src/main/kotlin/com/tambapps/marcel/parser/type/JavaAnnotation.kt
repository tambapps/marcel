package com.tambapps.marcel.parser.type

import kotlin.reflect.KClass

interface JavaAnnotation {

  data class Attribute(val name: String, val type: JavaType, val defaultValue: Any?)

  val attributes: List<Attribute>
  val type: JavaType
  val descriptor get() = type.descriptor

  companion object {
    fun of (klass: KClass<*>) = of(klass.java)

    fun of (clazz: Class<*>) = of(JavaType.of(clazz))
    fun of (javaType: JavaType) = LoadedJavaAnnotation(javaType)
  }
}

class LoadedJavaAnnotation(override val type: JavaType): JavaAnnotation {

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
