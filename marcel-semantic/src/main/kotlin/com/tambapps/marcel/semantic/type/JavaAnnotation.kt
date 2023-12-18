package com.tambapps.marcel.semantic.type

import java.lang.annotation.ElementType
import kotlin.reflect.KClass

// TODO rename JavaAnnotationType
interface JavaAnnotation: JavaType {

  companion object {
    fun of (klass: KClass<*>) = of(klass.java)

    fun of(clazz: Class<*>) = LoadedJavaAnnotation(clazz)
    fun of(javaType: JavaType) = of(javaType.realClazz)
  }

  data class Attribute(val name: String, val type: JavaType, val defaultValue: Any?)

  val attributes: List<Attribute>

  val targets: List<ElementType>

  override val asAnnotationType: JavaAnnotation
    get() = this
}
