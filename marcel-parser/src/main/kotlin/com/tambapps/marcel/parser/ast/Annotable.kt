package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaType

interface Annotable {

    val annotations: List<AnnotationNode>

    fun getAnnotation(javaType: JavaType): AnnotationNode? {
        return annotations.find { it.javaAnnotation.type == javaType }
    }

}