package com.tambapps.marcel.semantic.symbol.type.annotation

import com.tambapps.marcel.semantic.symbol.type.JavaAnnotationType

class LoadedJavaAnnotation(
  override val type: JavaAnnotationType,
  private val attributes: Map<String, JavaAnnotation.Attribute>
) : JavaAnnotation {
  override fun getAttribute(name: String): JavaAnnotation.Attribute? = attributes[name]
}