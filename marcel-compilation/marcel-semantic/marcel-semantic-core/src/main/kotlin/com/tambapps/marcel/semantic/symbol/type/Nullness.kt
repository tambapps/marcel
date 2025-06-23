package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.semantic.symbol.NullAware
import org.jspecify.annotations.NonNull
import org.jspecify.annotations.NullMarked
import org.jspecify.annotations.Nullable
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

    fun of(field: Field) = when {
      field.type.isPrimitive -> NOT_NULL
      field.getAnnotation(Nullable::class.java) != null -> NULLABLE
      field.getAnnotation(NonNull::class.java) != null -> NOT_NULL
      field.getAnnotation(NullMarked::class.java) != null
          || field.declaringClass.getAnnotation(NullMarked::class.java) != null
          || field.declaringClass.module.getAnnotation(NullMarked::class.java) != null
      -> if (field.getAnnotation(Nullable::class.java) != null) NULLABLE else NOT_NULL
      else -> UNKNOWN
    }

    fun of(method: Method) = when {
      method.returnType.isPrimitive -> NOT_NULL
      method.getAnnotation(Nullable::class.java) != null -> NULLABLE
      method.getAnnotation(NonNull::class.java) != null -> NOT_NULL
      method.getAnnotation(NullMarked::class.java) != null
          || method.declaringClass.getAnnotation(NullMarked::class.java) != null
          || method.declaringClass.module.getAnnotation(NullMarked::class.java) != null
        -> if (method.getAnnotation(Nullable::class.java) != null) NULLABLE else NOT_NULL
      else -> UNKNOWN
    }

    fun of(parameter: Parameter, declaringClass: Class<*>) = when {
      parameter.type.isPrimitive -> NOT_NULL
      parameter.getAnnotation(Nullable::class.java) != null -> NULLABLE
      parameter.getAnnotation(NonNull::class.java) != null -> NOT_NULL
      declaringClass.getAnnotation(NullMarked::class.java) != null
          || declaringClass.getAnnotation(NullMarked::class.java) != null
          || declaringClass.module.getAnnotation(NullMarked::class.java) != null
        -> if (parameter.getAnnotation(Nullable::class.java) != null) NULLABLE else NOT_NULL
      else -> UNKNOWN
    }
  }
}