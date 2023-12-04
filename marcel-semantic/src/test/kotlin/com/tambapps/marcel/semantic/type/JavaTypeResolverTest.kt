package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.ExtensionJavaMethod
import com.tambapps.marcel.semantic.method.JavaMethodImpl
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import marcel.lang.lambda.Lambda
import marcel.lang.methods.DefaultMarcelMethods
import marcel.lang.primitives.collections.IntCollection
import marcel.lang.primitives.collections.lists.IntList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.function.Function
import java.util.function.IntPredicate
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
  fun getMethodWithLambdaParameterOfArray() {
    val stringArrayType = Array<String>::class.javaType
    val method = typeResolver.findMethod(stringArrayType, "map", listOf(Lambda::class.javaType))
    assertTrue(method is ExtensionJavaMethod)
    method as ExtensionJavaMethod
    assertEquals(ReflectJavaMethod(DefaultMarcelMethods::class.java.getDeclaredMethod("map", Array<Any>::class.java,Function::class.java)), method.actualMethod)
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

  @Test
  fun testLambdaArgMatch() {
    val method = JavaMethodImpl(
      ownerClass = JavaType.Object,
      visibility = Visibility.PUBLIC,
      name = "assertThrows",
      parameters = listOf(MethodParameter(type = Class::class.javaType, name = "clazz"), MethodParameter(type = Runnable::class.javaType, name = "runnable")),
      returnType = Class::class.javaType
    )
    typeResolver.defineMethod(method.ownerClass, method)
    assertEquals(method,
      typeResolver.findMethod(JavaType.Object, "assertThrows", listOf(Class::class.javaType, Lambda::class.javaType)))
  }

  @Test
  fun testExtensionMethodPriority() {
    assertEquals(ReflectJavaMethod(IntCollection::class.java.getDeclaredMethod("any", IntPredicate::class.java)),
      typeResolver.findMethod(IntList::class.javaType, "any", listOf(Lambda::class.javaType)))

  }
}