package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.parser.type.JavaAnnotation
import com.tambapps.marcel.parser.type.JavaType
import kotlin.reflect.KClass


val KClass<*>.javaType get() = JavaType.of(this.java)
val Class<*>.javaType get() = JavaType.of(this)
val KClass<*>.javaAnnotation get() = JavaAnnotation.of(this)
val Class<*>.javaAnnotation get() = JavaAnnotation.of(this)