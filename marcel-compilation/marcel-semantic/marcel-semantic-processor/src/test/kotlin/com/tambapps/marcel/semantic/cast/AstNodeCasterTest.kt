package com.tambapps.marcel.semantic.cast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AstNodeCasterTest {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
  }

  private val caster = AstNodeCaster(TYPE_RESOLVER)


  @Test
  fun testCastPrimitiveToPrimitive() {
    assertEquals(cast(JavaType.double, float(2.0f)), caster.cast(JavaType.double, float(2.0f)))
    assertEquals(cast(JavaType.float, double(0.2)), caster.cast(JavaType.float, double(0.2)))
    assertEquals(cast(JavaType.long, int(6)), caster.cast(JavaType.long, int(6)))
    assertEquals(cast(JavaType.double, int(6)), caster.cast(JavaType.double, int(6)))


  }
  @Test
  fun testNoCast() {
    var node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode = double(2.0)
    assertEquals(node, caster.cast(JavaType.double, node))

    node = int(2)
    assertEquals(node, caster.cast(JavaType.int, node))

    node = node(JavaType.Object)
    assertEquals(node, caster.cast(JavaType.Object, node))

  }
  @Test
  fun testCastDynamicObject() {
    var node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode = int(2)
    assertEquals(fCall(JavaType.DynamicObject, "of", listOf(
      fCall(JavaType.Integer, "valueOf", listOf(node), node)
    ), node), caster.cast(JavaType.DynamicObject, node))

    assertNotEquals(fCall(JavaType.DynamicObject, "of", listOf(
      fCall(JavaType.Integer, "valueOf", listOf(int(4)), node)
    ), node), caster.cast(JavaType.DynamicObject, node))

    node = node(JavaType.IntRange)
    assertEquals(fCall(JavaType.DynamicObject, "of", listOf(node), node), caster.cast(JavaType.DynamicObject, node))
  }

  @Test
  fun testCastObjectToObject() {
    val node = node(Number::class.javaType)
    assertEquals(JavaCastNode(JavaType.Integer, node, token()), caster.cast(JavaType.Integer, node))
    assertEquals(JavaCastNode(JavaType.Integer, node, token()), caster.cast(JavaType.Integer, node))

    // no need to cast anything
    assertEquals(node, caster.cast(Number::class.javaType, node))
    assertEquals(node, caster.cast(JavaType.Object, node))
  }

  @Test
  fun testCastPrimitiveToObject() {
    var node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode = int(2)

    assertEquals(fCall(JavaType.Integer, "valueOf", listOf(node), node), caster.cast(JavaType.Integer, node))
    assertEquals(fCall(JavaType.Integer, "valueOf", listOf(node), int(2)), caster.cast(JavaType.Integer, node))
    assertEquals(fCall(JavaType.Integer, "valueOf", listOf(node), int(2)), caster.cast(JavaType.Object, node))
    assertNotEquals(fCall(JavaType.Integer, "valueOf", listOf(node), int(3)), caster.cast(JavaType.int, node))

    node = long(1L)
    assertEquals(fCall(JavaType.Long, "valueOf", listOf(node), node), caster.cast(JavaType.Long, node))

    node = float(1f)
    assertEquals(fCall(JavaType.Float, "valueOf", listOf(node), node), caster.cast(JavaType.Float, node))

    node = double(1.0)
    assertEquals(fCall(JavaType.Double, "valueOf", listOf(node), node), caster.cast(JavaType.Double, node))

    node = char('1')
    assertEquals(fCall(JavaType.Character, "valueOf", listOf(node), node), caster.cast(JavaType.Character, node))

    node = bool(true)
    assertEquals(fCall(JavaType.Boolean, "valueOf", listOf(node), node), caster.cast(JavaType.Boolean, node))

    assertThrows<MarcelSemanticException> { caster.cast(JavaType.String, node) }
  }

  @Test
  fun testCastObjectToPrimitive() {
    var node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode = node(JavaType.Integer)
    assertEquals(fCall(JavaType.Integer, "intValue", emptyList(), node), caster.cast(JavaType.int, node))

    node = node(JavaType.Long)
    assertEquals(fCall(JavaType.Long, "longValue", emptyList(), node), caster.cast(JavaType.long, node))

    node = node(JavaType.Float)
    assertEquals(fCall(JavaType.Float, "floatValue", emptyList(), node), caster.cast(JavaType.float, node))

    node = node(JavaType.Double)
    assertEquals(fCall(JavaType.Double, "doubleValue", emptyList(), node), caster.cast(JavaType.double, node))

    node = node(JavaType.Character)
    assertEquals(fCall(JavaType.Character, "charValue", emptyList(), node), caster.cast(JavaType.char, node))

    node = node(JavaType.Boolean)
    assertEquals(fCall(JavaType.Boolean, "booleanValue", emptyList(), node), caster.cast(JavaType.boolean, node))
  }

  private fun node(type: JavaType) = MyExpressionNode(type)

  private inner class MyExpressionNode(type: JavaType) : com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(type, token()) {
    override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = throw UnsupportedOperationException()

  }

  private fun cast(type: JavaType, node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) = JavaCastNode(type, node, token())
  private fun fCall(ownerType: JavaType, name: String, arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>, node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) = caster.functionCall(ownerType, name, arguments, node)
  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())
  private fun char(value: Char) = CharConstantNode(value = value, token = token())
  private fun bool(value: Boolean) = BoolConstantNode(value = value, token = token())
  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

}