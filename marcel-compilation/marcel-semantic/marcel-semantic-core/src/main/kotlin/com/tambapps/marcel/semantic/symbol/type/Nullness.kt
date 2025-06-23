package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.semantic.symbol.NullAware
import org.jspecify.annotations.NonNull
import org.jspecify.annotations.NullMarked
import org.jspecify.annotations.Nullable
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter


enum class Nullness {
  /**
   * The value can be null
   */
  NULLABLE,
  /**
   * The value can't be null
   */
  NOT_NULL,
  /**
   * The value may or may not be null
   */
  UNKNOWN;


  companion object {
    fun of(isNullable: Boolean) = if (isNullable) NULLABLE else NOT_NULL

    fun of(nullAware1: NullAware, nullAware2: NullAware) = of(nullAware1.nullness, nullAware2.nullness)
    fun of(nullness1: Nullness, nullness2: Nullness) = when {
      nullness1 == NULLABLE || nullness2 == NULLABLE -> NULLABLE
      nullness1 == UNKNOWN || nullness2 == UNKNOWN -> UNKNOWN
      else -> NOT_NULL
    }

    fun of(field: Field) = of(field.annotatedType, field.type, field.declaringClass)

    fun of(method: Method) = of(method.annotatedReturnType, method.returnType, method.declaringClass)

    fun of(parameter: Parameter, declaringClass: Class<*>) = of(parameter.annotatedType, parameter.type, declaringClass)

    private fun of(element: AnnotatedElement, elementType: Class<*>, declaringClass: Class<*>) = when {
      elementType.isPrimitive -> NOT_NULL
      element.getAnnotation(Nullable::class.java) != null -> NULLABLE
      element.getAnnotation(NonNull::class.java) != null -> NOT_NULL
      element.getAnnotation(NullMarked::class.java) != null
          || declaringClass.getAnnotation(NullMarked::class.java) != null
          || declaringClass.module.getAnnotation(NullMarked::class.java) != null
        -> if (element.getAnnotation(Nullable::class.java) != null) NULLABLE else NOT_NULL
      else -> UNKNOWN
    }
  }
}