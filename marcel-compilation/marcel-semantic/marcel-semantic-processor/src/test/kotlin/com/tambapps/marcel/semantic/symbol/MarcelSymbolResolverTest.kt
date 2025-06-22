package com.tambapps.marcel.semantic.symbol

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.method.CastMethod
import com.tambapps.marcel.semantic.symbol.method.ExtensionMarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.symbol.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import marcel.lang.lambda.Lambda
import marcel.lang.methods.DefaultMarcelMethods
import marcel.util.primitives.collections.IntCollection
import marcel.util.primitives.collections.lists.IntList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.function.Function
import java.util.function.IntPredicate
import java.util.stream.Stream

class MarcelSymbolResolverTest {

  companion object {
    val StringBuilder = java.lang.StringBuilder::class.javaType
  }

  private val symbolResolver = MarcelSymbolResolver()

  @Test
  fun testVarArg() {
    val joinMethod = ReflectJavaMethod(java.lang.String::class.java.getMethod("join", CharSequence::class.java, Array<CharSequence>::class.java))
    assertTrue(joinMethod.isVarArgs)

    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String, JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, CharSequence::class.javaType.arrayType)))

    Assertions.assertFalse(symbolResolver.matches(joinMethod, emptyList()))
    Assertions.assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.int)))
    Assertions.assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String, JavaType.int)))
    Assertions.assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, Int::class.javaType.arrayType)))
  }

  @Test
  fun testListArgMatchesArray() {
    val method = method(name = "foo", parameters = listOf(MethodParameter(JavaType.List, "bar")), returnType = JavaType.void)
    for (arrayType in JavaType.ARRAYS) assertTrue(symbolResolver.matches(method, listOf(arrayType)))
    assertFalse(symbolResolver.matches(method, listOf(JavaType.Object)))
    // primitive matches
    for (primitiveType in listOf(JavaType.int, JavaType.long, JavaType.float, JavaType.double, JavaType.char)) {
      assertTrue(symbolResolver.matches(
        method(name = "foo",
          parameters = listOf(MethodParameter(JavaType.PRIMITIVE_LIST_MAP.getValue(primitiveType), "bar")),
          returnType = JavaType.void)
        , listOf(primitiveType.arrayType)))
    }
  }

  @Test
  fun testSetArgMatchesArray() {
    val method = method(name = "foo", parameters = listOf(MethodParameter(JavaType.Set, "bar")), returnType = JavaType.void)
    for (arrayType in JavaType.ARRAYS) assertTrue(symbolResolver.matches(method, listOf(arrayType)))
    assertFalse(symbolResolver.matches(method, listOf(JavaType.Object)))
    // primitive matches
    for (primitiveType in listOf(JavaType.int, JavaType.long, JavaType.float, JavaType.double, JavaType.char)) {
      assertTrue(symbolResolver.matches(
        method(name = "foo",
          parameters = listOf(MethodParameter(JavaType.PRIMITIVE_SET_MAP.getValue(primitiveType), "bar")),
          returnType = JavaType.void)
        , listOf(primitiveType.arrayType)))
    }
  }

  @Test
  fun getMethodWithLambdaParameter() {
    val method = symbolResolver.findMethod(Stream::class.javaType, "map", listOf(Lambda::class.javaType))
    assertEquals(ReflectJavaMethod(Stream::class.java.getDeclaredMethod("map", Function::class.java)), method)
  }

  @Test
  fun getMethodWithLambdaParameterOfArray() {
    val stringArrayType = Array<String>::class.javaType
    val method = symbolResolver.findMethod(stringArrayType, "map", listOf(Lambda::class.javaType))
    assertTrue(method is ExtensionMarcelMethod, "Expected an extension method but got ${method?.javaClass}")
    method as ExtensionMarcelMethod
    assertEquals(ReflectJavaMethod(DefaultMarcelMethods::class.java.getDeclaredMethod("map", Array<Any>::class.java,Function::class.java)), method.actualMethod)
  }

  @Test
  fun getMethodWithMultipleSignature() {
    // primitive types
    assertEquals(
      ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Int::class.java)),
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.int)))
    assertEquals(
      ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Float::class.java)),
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.float)))
    assertEquals(
      ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Long::class.java)),
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.long)))
    assertEquals(
      ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", Char::class.java)),
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.char)))

    // object types
    val expectedMethodObject = ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", JavaType.Object.realClazz))
    assertEquals(expectedMethodObject,
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.Object)))
    assertEquals(expectedMethodObject,
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.IntRange)))
    assertEquals(expectedMethodObject,
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.Integer)))
    assertEquals(
      ReflectJavaMethod(java.lang.StringBuilder::class.java.getDeclaredMethod("append", String::class.java)),
      symbolResolver.findMethod(StringBuilder, "append", listOf(JavaType.String)))
  }

  @Test
  fun getSuperMethodOfInterface() {
    assertEquals(
      ReflectJavaMethod(Any::class.java.getDeclaredMethod("toString")),
      symbolResolver.findMethod(JavaType.DynamicObject, "toString", emptyList()))
  }

  @Test
  fun testLambdaArgMatch() {
    val method = MarcelMethodImpl(
      ownerClass = JavaType.Object,
      visibility = Visibility.PUBLIC,
      name = "assertThrows",
      parameters = listOf(MethodParameter(type = Class::class.javaType, name = "clazz"), MethodParameter(type = Runnable::class.javaType, name = "runnable")),
      returnType = Class::class.javaType
    )
    symbolResolver.defineMethod(method.ownerClass, method)
    assertEquals(method,
      symbolResolver.findMethod(JavaType.Object, "assertThrows", listOf(Class::class.javaType, Lambda::class.javaType)))
  }

  @Test
  fun testExtensionMethodPriority() {
    assertEquals(
      ReflectJavaMethod(IntCollection::class.java.getDeclaredMethod("any", IntPredicate::class.java)),
      symbolResolver.findMethod(IntList::class.javaType, "any", listOf(Lambda::class.javaType)))
  }

  @Test
  fun testCastPrimitive() {
    assertEquals(CastMethod.METHODS.find { it.returnType == JavaType.int },
      symbolResolver.findMethod(JavaType.Object, "cast", listOf(JavaType.int)))
  }

  @Test
  fun testCastObject() {
    assertEquals(CastMethod.METHODS.find { it.returnType == JavaType.Object },
      symbolResolver.findMethod(JavaType.Object, "cast", listOf(JavaType.Object)))
  }

  private fun method(visibility: Visibility = Visibility.PUBLIC, ownerType: JavaType = JavaType.Object, name: String, parameters: List<MethodParameter>, returnType: JavaType): MarcelMethod {
    return MarcelMethodImpl(ownerClass = ownerType, visibility = visibility, name = name, parameters = parameters, returnType = returnType)
  }
}