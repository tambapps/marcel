package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import kotlin.reflect.KClass

class JavaPrimitiveType internal constructor(
  objectKlazz: KClass<*>,
  val defaultValueGenerator: (LexToken) -> ExpressionNode,
): LoadedJavaType(objectKlazz.javaPrimitiveType!!, emptyList()) {

  override fun getDefaultValueExpression(token: LexToken): ExpressionNode = defaultValueGenerator(token)

  override val visibility = Visibility.PUBLIC
  override val packageName = null
  val objectClass = objectKlazz.java
  override val objectType: JavaType
    get() = JavaType.of(objectClass)
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaPrimitiveType {
    if (genericTypes.isNotEmpty()) throw MarcelSemanticException("Cannot have primitive type with generic types")
    return this
  }

  override val asPrimitiveType: JavaPrimitiveType
    get() = this
  override fun raw(): JavaType {
    return this
  }
}