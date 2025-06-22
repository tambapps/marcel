package com.tambapps.marcel.semantic.processor.extensions

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.symbol.type.JavaType

fun JavaType.getDefaultValueExpression(it: LexToken): ExpressionNode {
  if (!primitive) return NullValueNode(it)
  return when (this) {
    JavaType.int -> IntConstantNode(it, value = 0)
    JavaType.float -> FloatConstantNode(it, value = 0f)
    JavaType.long -> LongConstantNode(it, value = 0L)
    JavaType.double -> DoubleConstantNode(it, value = 0.0)
    JavaType.char -> CharConstantNode(it, value = 0.toChar())
    JavaType.boolean -> BoolConstantNode(it, value = false)
    JavaType.short -> ShortConstantNode(it, value = 0)
    JavaType.byte -> ByteConstantNode(it, value = 0)
    JavaType.void -> VoidExpressionNode(it)
    else -> throw RuntimeException("Unexpected error, got type ${this.javaClass}")
  }
}