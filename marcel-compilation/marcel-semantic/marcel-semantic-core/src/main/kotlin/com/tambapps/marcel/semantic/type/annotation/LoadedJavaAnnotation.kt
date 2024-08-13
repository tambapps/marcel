package com.tambapps.marcel.semantic.type.annotation

import com.tambapps.marcel.semantic.type.JavaAnnotationType

class LoadedJavaAnnotation(
  override val type: JavaAnnotationType,
  private val attributes: Map<String, JavaAnnotation.Attribute>
) : JavaAnnotation {
  override fun getAttribute(name: String): JavaAnnotation.Attribute? = attributes[name]
}