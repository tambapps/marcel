package com.tambapps.marcel.semantic.type

interface JavaAnnotation: JavaTyped {

  override val type: JavaAnnotationType

  fun getAttribute(name: String): Attribute?

  data class Attribute(val name: String, val type: JavaType, val value: Any)

}