package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.type.JavaType

interface Annotable {

    val annotations: List<AnnotationNode>

    fun getAnnotation(javaType: JavaType): AnnotationNode? {
        return annotations.find { it.type == javaType }
    }

}