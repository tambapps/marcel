package com.tambapps.marcel.semantic.type

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

  val targets: List<ElementType>
}
