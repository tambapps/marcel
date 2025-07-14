package com.tambapps.marcel.semantic.processor.cast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
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
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AstNodeCasterTest {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
  }

  private val caster = AstNodeCaster(TYPE_RESOLVER)


  @Test
  fun testCastPrimitiveToPrimitive() {
    Assertions.assertEquals(
      cast(JavaType.Companion.double, float(2.0f)),
      caster.cast(JavaType.Companion.double, float(2.0f))
    )
    Assertions.assertEquals(
      cast(JavaType.Companion.float, double(0.2)),
      caster.cast(JavaType.Companion.float, double(0.2))
    )
    Assertions.assertEquals(cast(JavaType.Companion.long, int(6)), caster.cast(JavaType.Companion.long, int(6)))
    Assertions.assertEquals(cast(JavaType.Companion.double, int(6)), caster.cast(JavaType.Companion.double, int(6)))


  }
  @Test
  fun testNoCast() {
    var node: ExpressionNode = double(2.0)
    Assertions.assertEquals(node, caster.cast(JavaType.Companion.double, node))

    node = int(2)
    Assertions.assertEquals(node, caster.cast(JavaType.Companion.int, node))

    node = node(JavaType.Companion.Object)
    Assertions.assertEquals(node, caster.cast(JavaType.Companion.Object, node))

  }
  @Test
  fun testCastDynamicObject() {
    var node: ExpressionNode = int(2)
    Assertions.assertEquals(
      fCall(
        JavaType.Companion.DynamicObject, "of", listOf(
          fCall(JavaType.Companion.Integer, "valueOf", listOf(node), node)
        ), node
      ), caster.cast(JavaType.Companion.DynamicObject, node)
    )

    Assertions.assertNotEquals(
      fCall(
        JavaType.Companion.DynamicObject, "of", listOf(
          fCall(JavaType.Companion.Integer, "valueOf", listOf(int(4)), node)
        ), node
      ), caster.cast(JavaType.Companion.DynamicObject, node)
    )

    node = node(JavaType.Companion.IntRange)
    Assertions.assertEquals(
      fCall(JavaType.Companion.DynamicObject, "of", listOf(node), node),
      caster.cast(JavaType.Companion.DynamicObject, node)
    )
  }

  @Test
  fun testCastObjectToObject() {
    val node = node(Number::class.javaType)
    Assertions.assertEquals(
      JavaCastNode(JavaType.Companion.Integer, node, token()),
      caster.cast(JavaType.Companion.Integer, node)
    )
    Assertions.assertEquals(
      JavaCastNode(JavaType.Companion.Integer, node, token()),
      caster.cast(JavaType.Companion.Integer, node)
    )

    // no need to cast anything
    Assertions.assertEquals(node, caster.cast(Number::class.javaType, node))
    Assertions.assertEquals(node, caster.cast(JavaType.Companion.Object, node))
  }

  @Test
  fun testCastPrimitiveToObject() {
    var node: ExpressionNode = int(2)

    Assertions.assertEquals(
      fCall(JavaType.Companion.Integer, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Integer, node)
    )
    Assertions.assertEquals(
      fCall(JavaType.Companion.Integer, "valueOf", listOf(node), int(2)),
      caster.cast(JavaType.Companion.Integer, node)
    )
    Assertions.assertEquals(
      fCall(JavaType.Companion.Integer, "valueOf", listOf(node), int(2)),
      caster.cast(JavaType.Companion.Object, node)
    )
    Assertions.assertNotEquals(
      fCall(JavaType.Companion.Integer, "valueOf", listOf(node), int(3)),
      caster.cast(JavaType.Companion.int, node)
    )

    node = long(1L)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Long, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Long, node)
    )

    node = float(1f)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Float, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Float, node)
    )

    node = double(1.0)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Double, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Double, node)
    )

    node = char('1')
    Assertions.assertEquals(
      fCall(JavaType.Companion.Character, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Character, node)
    )

    node = bool(true)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Boolean, "valueOf", listOf(node), node),
      caster.cast(JavaType.Companion.Boolean, node)
    )

    assertThrows<MarcelSemanticException> { caster.cast(JavaType.Companion.String, node) }
  }

  @Test
  fun testCastObjectToPrimitive() {
    var node: ExpressionNode = node(JavaType.Companion.Integer)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Integer, "intValue", emptyList(), node),
      caster.cast(JavaType.Companion.int, node)
    )

    node = node(JavaType.Companion.Long)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Long, "longValue", emptyList(), node),
      caster.cast(JavaType.Companion.long, node)
    )

    node = node(JavaType.Companion.Float)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Float, "floatValue", emptyList(), node),
      caster.cast(JavaType.Companion.float, node)
    )

    node = node(JavaType.Companion.Double)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Double, "doubleValue", emptyList(), node),
      caster.cast(JavaType.Companion.double, node)
    )

    node = node(JavaType.Companion.Character)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Character, "charValue", emptyList(), node),
      caster.cast(JavaType.Companion.char, node)
    )

    node = node(JavaType.Companion.Boolean)
    Assertions.assertEquals(
      fCall(JavaType.Companion.Boolean, "booleanValue", emptyList(), node),
      caster.cast(JavaType.Companion.boolean, node)
    )
  }

  private fun node(type: JavaType) = MyExpressionNode(type)

  private inner class MyExpressionNode(override val type: JavaType) : AbstractExpressionNode(token()) {

    override val nullness: Nullness
      get() = Nullness.UNKNOWN
    override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = throw UnsupportedOperationException()

  }

  private fun cast(type: JavaType, node: ExpressionNode) = JavaCastNode(type, node, token())
  private fun fCall(ownerType: JavaType, name: String, arguments: List<ExpressionNode>, node: ExpressionNode) = caster.functionCall(ownerType, name, arguments, node)
  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())
  private fun char(value: Char) = CharConstantNode(value = value, token = token())
  private fun bool(value: Boolean) = BoolConstantNode(value = value, token = token())
  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

}