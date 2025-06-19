package com.tambapps.marcel.semantic.processor.cast

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.type.JavaType

interface ExpressionCaster {

  fun truthyCast(node: ExpressionNode): ExpressionNode

  /**
   * Cast the provided node (if necessary) so that it fits the expected type.
   * Throws a MarcelSemanticException in case of casting failure
   */
  fun cast(expectedType: JavaType, node: ExpressionNode): ExpressionNode

  fun javaCast(expectedType: JavaType, node: ExpressionNode): ExpressionNode

  fun castNumberConstantOrNull(value: Int, type: JavaPrimitiveType): Any? = when (type) {
    JavaType.byte -> value.toByte()
    JavaType.int -> value
    JavaType.long -> value.toLong()
    JavaType.float -> value.toFloat()
    JavaType.double -> value.toDouble()
    JavaType.short -> value.toShort()
    else -> null
  }
}