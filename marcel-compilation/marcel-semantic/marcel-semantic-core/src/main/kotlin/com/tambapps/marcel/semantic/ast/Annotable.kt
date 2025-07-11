package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.symbol.type.JavaType

/**
 * An object that can be annotated
 */
interface Annotable {

  val annotations: List<AnnotationNode>

  fun getAnnotation(javaType: JavaType): AnnotationNode? {
    return annotations.find { it.type == javaType }
  }

}