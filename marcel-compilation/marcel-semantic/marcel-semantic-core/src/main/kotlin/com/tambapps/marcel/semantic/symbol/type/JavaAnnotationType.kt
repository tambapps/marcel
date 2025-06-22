package com.tambapps.marcel.semantic.symbol.type

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * [JavaType] representing an annotation type
 */
interface JavaAnnotationType: JavaType {

  companion object {
    fun of (klass: KClass<*>) = of(klass.java)

    fun of(clazz: Class<*>) = JavaType.of(clazz) as JavaAnnotationType
    fun of(javaType: JavaType) = of(javaType.realClazz)
  }

  data class Attribute(val name: String, val type: JavaType, val defaultValue: Any?)

  val attributes: List<Attribute>

  val targets: List<ElementType>
  val retentionPolicy: RetentionPolicy

  override val asAnnotationType: JavaAnnotationType
    get() = this
}
