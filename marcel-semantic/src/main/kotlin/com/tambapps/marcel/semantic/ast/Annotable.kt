package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.type.JavaType

/**
 * An object that can be annotated
 */
interface Annotable {

    val annotations: List<AnnotationNode>

    fun getAnnotation(javaType: JavaType): AnnotationNode? {
        return annotations.find { it.annotationType == javaType }
    }

}