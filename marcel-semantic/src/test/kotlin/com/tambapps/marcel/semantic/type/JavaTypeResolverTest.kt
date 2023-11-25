package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import marcel.lang.lambda.Lambda
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.function.Function
import java.util.stream.Stream

class JavaTypeResolverTest {

  companion object {
    val StringBuilder = java.lang.StringBuilder::class.javaType
  }

  private val typeResolver = JavaTypeResolver()


  @Test
  fun getMethodWithLambdaParameter() {
    val method = typeResolver.findMethod(Stream::class.javaType, "map", listOf(Lambda::class.javaType))
    assertEquals(ReflectJavaMethod(Stream::class.java.getDeclaredMethod("map", Function::class.java)), method)
  }

  @Test
  fun getMethodWithMultipleSignature() {
    // primitive types
    assertEquals(ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Int::class.java)),
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.int)))
    assertEquals(ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Float::class.java)),
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.float)))
    assertEquals(ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Long::class.java)),
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.long)))
    assertEquals(ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Char::class.java)),
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.char)))

    // object types
    val expectedMethodObject = ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", JavaType.Object.realClazz))
    assertEquals(expectedMethodObject,
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.Object)))
    assertEquals(expectedMethodObject,
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.IntRange)))
    assertEquals(expectedMethodObject,
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.Integer)))
    assertEquals(ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", String::class.java)),
      typeResolver.findMethod(StringBuilder, "append", listOf(JavaType.String)))
  }

  @Test
  fun getSuperMethodOfInterface() {
    assertEquals(ReflectJavaMethod(Any::class.java.getDeclaredMethod("toString")),
      typeResolver.findMethod(JavaType.DynamicObject, "toString", emptyList()))


  }
}