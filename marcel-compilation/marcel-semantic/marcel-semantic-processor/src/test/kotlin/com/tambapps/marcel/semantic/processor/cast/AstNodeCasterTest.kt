package com.tambapps.marcel.semantic.processor.cast

import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.compose.ExpressionComposer
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AstNodeCasterTest: ExpressionComposer(caster = AstNodeCaster(TYPE_RESOLVER)) {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
  }

  @Test
  fun testCastPrimitiveToPrimitive() {
    Assertions.assertEquals(
      cast(JavaType.double, float(2.0f)),
      caster.cast(JavaType.double, float(2.0f))
    )
    Assertions.assertEquals(
      cast(JavaType.float, double(0.2)),
      caster.cast(JavaType.float, double(0.2))
    )
    Assertions.assertEquals(cast(JavaType.long, int(6)), caster.cast(JavaType.long, int(6)))
    Assertions.assertEquals(cast(JavaType.double, int(6)), caster.cast(JavaType.double, int(6)))
  }

  @Test
  fun testNoCast() {
    var node: ExpressionNode = double(2.0)
    Assertions.assertEquals(node, caster.cast(JavaType.double, node))

    node = int(2)
    Assertions.assertEquals(node, caster.cast(JavaType.int, node))

    node = node(JavaType.Object)
    Assertions.assertEquals(node, caster.cast(JavaType.Object, node))

  }
  @Test
  fun testCastDynamicObject() {
    var node: ExpressionNode = int(2)
    Assertions.assertEquals(
      fCall(
        ownerType = JavaType.DynamicObject, name = "of", arguments = listOf(
          fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(node))
        )
      ), caster.cast(JavaType.DynamicObject, node)
    )

    Assertions.assertNotEquals(
      fCall(
        ownerType = JavaType.DynamicObject, name = "of", arguments = listOf(
          fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(int(4)))
        )
      ), caster.cast(JavaType.DynamicObject, node)
    )

    node = node(JavaType.IntRange)
    Assertions.assertEquals(
      fCall(ownerType = JavaType.DynamicObject, name = "of", arguments = listOf(node)),
      caster.cast(JavaType.DynamicObject, node)
    )
  }

  @Test
  fun testCastObjectToObject() {
    val node = node(Number::class.javaType)
    Assertions.assertEquals(
      javaCast(node, JavaType.Integer),
      caster.cast(JavaType.Integer, node)
    )
    Assertions.assertEquals(
      javaCast(node, JavaType.Integer),
      caster.cast(JavaType.Integer, node)
    )

    // no need to cast anything
    Assertions.assertEquals(node, caster.cast(Number::class.javaType, node))
    Assertions.assertEquals(node, caster.cast(JavaType.Object, node))
  }

  @Test
  fun testCastPrimitiveToObject() {
    var node: ExpressionNode = int(2)

    Assertions.assertEquals(
      fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Integer, node)
    )
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Integer, node)
    )
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Object, node)
    )
    Assertions.assertNotEquals(
      fCall(ownerType = JavaType.Integer, name = "valueOf", arguments = listOf(int(3))),
      caster.cast(JavaType.int, node)
    )

    node = long(1L)
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Long, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Long, node)
    )

    node = float(1f)
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Float, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Float, node)
    )

    node = double(1.0)
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Double, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Double, node)
    )

    node = char('1')
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Character, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Character, node)
    )

    node = bool(true)
    Assertions.assertEquals(
      fCall(ownerType = JavaType.Boolean, name = "valueOf", arguments = listOf(node)),
      caster.cast(JavaType.Boolean, node)
    )

    assertThrows<MarcelSemanticException> { caster.cast(JavaType.String, node) }
  }

  @Test
  fun testCastObjectToPrimitive() {
    var node: ExpressionNode = node(JavaType.Integer)
    Assertions.assertEquals(
      fCall(owner = node, name = "intValue"),
      caster.cast(JavaType.int, node)
    )

    node = node(JavaType.Long)
    Assertions.assertEquals(
      fCall(owner = node, name = "longValue"),
      caster.cast(JavaType.long, node)
    )

    node = node(JavaType.Float)
    Assertions.assertEquals(
      fCall(owner = node, name = "floatValue"),
      caster.cast(JavaType.float, node)
    )

    node = node(JavaType.Double)
    Assertions.assertEquals(
      fCall(owner = node, name = "doubleValue"),
      caster.cast(JavaType.double, node)
    )

    node = node(JavaType.Character)
    Assertions.assertEquals(
      fCall(owner = node, name = "charValue"),
      caster.cast(JavaType.char, node)
    )

    node = node(JavaType.Boolean)
    Assertions.assertEquals(
      fCall(owner = node, name = "booleanValue"),
      caster.cast(JavaType.boolean, node)
    )
  }

  private fun node(type: JavaType) = MyExpressionNode(type)

  private inner class MyExpressionNode(override val type: JavaType) : AbstractExpressionNode(tokenStart) {

    override val nullness: Nullness
      get() = Nullness.UNKNOWN
    override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = throw UnsupportedOperationException()

  }
}