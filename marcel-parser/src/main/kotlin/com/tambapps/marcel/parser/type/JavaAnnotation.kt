package com.tambapps.marcel.parser.type

import kotlin.reflect.KClass

interface JavaAnnotation {

  data class Attribute(val name: String, val type: JavaType, val defaultValue: Any?)

  val attributes: List<Attribute>
  val type: JavaType
  val descriptor get() = type.descriptor

  companion object {
    fun of (klass: KClass<*>) = of(klass.java)

    fun of (clazz: Class<*>) = LoadedJavaAnnotation(clazz)
  }
}

class LoadedJavaAnnotation(clazz: Class<*>): JavaAnnotation {

  override val type = LoadedObjectType(clazz)
  override val attributes = clazz.declaredMethods.map {
    JavaAnnotation.Attribute(it.name, LoadedObjectType(it.returnType), it.defaultValue)
  }

}
