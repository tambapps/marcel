package com.tambapps.marcel.semantic.ast.cast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewLambdaInstanceNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import marcel.lang.DynamicObject
import marcel.lang.MarcelTruth
import marcel.lang.runtime.BytecodeHelper

/**
 * Class transforming nodes if necessary in order to cast them.
 */
class AstNodeCaster(
  private val symbolResolver: MarcelSymbolResolver
) {

  fun truthyCast(node: ExpressionNode): ExpressionNode {
    return when (node.type) {
      JavaType.boolean -> node
      JavaType.Boolean -> cast(JavaType.boolean, node)
      else -> {
        if (node.type.primitive) throw MarcelSemanticException(node.token, "Cannot cast primitive into boolean")
        functionCall(MarcelTruth::class.javaType, "isTruthy", listOf(node), node)
      }
    }
  }

  // TODO rename smartCast
  /**
   * Cast the provided node (if necessary) so that it fits the expected type.
   * Throws a MarcelSemanticException in case of casting failure
   */
  fun cast(
    expectedType: JavaType,
    node: ExpressionNode
  ): ExpressionNode {
    val actualType = node.type
    return when {
      expectedType == actualType -> node
      expectedType == JavaType.DynamicObject ->
        if (actualType.implements(JavaType.DynamicObject)) node
        else functionCall(
          DynamicObject::class.javaType, "of", listOf(
            cast(JavaType.Object, node) // to handle primitives
          ), node
        )
      expectedType == JavaType.boolean -> truthyCast(node)
      actualType.primitive && expectedType.primitive -> primitiveToPrimitiveJavaCast(expectedType, node, actualType)
      expectedType.primitive && !actualType.primitive -> objectToPrimitiveJavaCast(expectedType, node, actualType)
      !expectedType.primitive && actualType.primitive -> primitiveToObjectJavaCast(expectedType, node, actualType)
      // Object to Object
      else -> when {
        expectedType.isExtendedOrImplementedBy(actualType) -> node
        actualType.isArray -> {
          when {
            // lists
            JavaType.intList.isAssignableFrom(expectedType) && actualType == JavaType.intArray -> functionCall(
              JavaType.intListImpl,
              "wrap",
              listOf(node),
              node
            )

            JavaType.longList.isAssignableFrom(expectedType) && actualType == JavaType.longArray -> functionCall(
              JavaType.longListImpl,
              "wrap",
              listOf(node),
              node
            )

            JavaType.floatList.isAssignableFrom(expectedType) && actualType == JavaType.floatArray -> functionCall(
              JavaType.floatListImpl,
              "wrap",
              listOf(node),
              node
            )

            JavaType.doubleList.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray -> functionCall(
              JavaType.doubleListImpl,
              "wrap",
              listOf(node),
              node
            )

            JavaType.charList.isAssignableFrom(expectedType) && actualType == JavaType.charArray -> functionCall(
              JavaType.charListImpl,
              "wrap",
              listOf(node),
              node
            )

            List::class.javaType.isAssignableFrom(expectedType) && actualType.isArray -> functionCall(
              BytecodeHelper::class.javaType,
              "createList",
              listOf(node),
              node
            )
            // sets
            JavaType.intSet.isAssignableFrom(expectedType) && actualType == JavaType.intArray
                || JavaType.longSet.isAssignableFrom(expectedType) && actualType == JavaType.longArray
                || JavaType.floatSet.isAssignableFrom(expectedType) && actualType == JavaType.floatArray
                || JavaType.doubleSet.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray
                || JavaType.characterSet.isAssignableFrom(expectedType) && actualType == JavaType.charArray
                || Set::class.javaType.isAssignableFrom(expectedType) && actualType.isArray -> functionCall(
              BytecodeHelper::class.javaType,
              "createSet",
              listOf(node),
              node
            )
            else -> incompatibleTypes(node, expectedType, actualType)
          }
        }

        expectedType.isInterface && node is NewLambdaInstanceNode -> {
          // effective cast check will be done later by the LambdaHandler
          node.lambdaNode.interfaceTypes.add(expectedType)
          node
        }

        !actualType.isAssignableFrom(expectedType) -> incompatibleTypes(node, expectedType, actualType)
        else -> JavaCastNode(expectedType, node, node.token)
      }
    }
  }

  fun javaCast(expectedType: JavaType, node: ExpressionNode): ExpressionNode {
    val actualType = node.type
    return when {
      actualType.primitive && expectedType.primitive -> primitiveToPrimitiveJavaCast(expectedType, node, actualType)
      expectedType.primitive && !actualType.primitive -> objectToPrimitiveJavaCast(expectedType, node, actualType)
      !expectedType.primitive && actualType.primitive -> primitiveToObjectJavaCast(expectedType, node, actualType)
      expectedType.isExtendedOrImplementedBy(actualType) -> node
      else -> JavaCastNode(expectedType, node, node.token)
    }
  }

  fun castNumberConstant(value: Int, type: JavaPrimitiveType, token: LexToken): Any = when (type) {
    JavaType.byte -> value.toByte()
    JavaType.int -> value
    JavaType.long -> value.toLong()
    JavaType.float -> value.toFloat()
    JavaType.double -> value.toDouble()
    JavaType.short -> value.toShort()
    else -> throw MarcelSemanticException(token, "Cannot convert value $value to $type")
  }

  private fun primitiveToPrimitiveJavaCast(expectedType: JavaType, node: ExpressionNode, actualType: JavaType): ExpressionNode {
    if (expectedType.asPrimitiveType.isNumber && !actualType.asPrimitiveType.isNumber
      || actualType.asPrimitiveType.isNumber && !expectedType.asPrimitiveType.isNumber
    ) incompatibleTypes(node, expectedType, actualType)
    return JavaCastNode(expectedType, node, node.token)
  }

  private fun objectToPrimitiveJavaCast(expectedType: JavaType, node: ExpressionNode, actualType: JavaType): ExpressionNode = when {
    expectedType == JavaType.boolean && actualType == JavaType.Boolean -> functionCall(
      JavaType.Boolean,
      "booleanValue",
      emptyList(),
      node
    )

    expectedType == JavaType.int && actualType == JavaType.Integer -> functionCall(
      JavaType.Integer,
      "intValue",
      emptyList(),
      node
    )

    expectedType == JavaType.char && actualType == JavaType.Character -> functionCall(
      JavaType.Character,
      "charValue",
      emptyList(),
      node
    )

    expectedType == JavaType.long && actualType == JavaType.Long -> functionCall(
      JavaType.Long,
      "longValue",
      emptyList(),
      node
    )

    expectedType == JavaType.float && actualType == JavaType.Float -> functionCall(
      JavaType.Float,
      "floatValue",
      emptyList(),
      node
    )

    expectedType == JavaType.double && actualType == JavaType.Double -> functionCall(
      JavaType.Double,
      "doubleValue",
      emptyList(),
      node
    )

    expectedType == JavaType.byte && actualType == JavaType.Byte -> functionCall(
      JavaType.Byte,
      "byteValue",
      emptyList(),
      node
    )

    expectedType == JavaType.short && actualType == JavaType.Short -> functionCall(
      JavaType.Short,
      "shortValue",
      emptyList(),
      node
    )

    expectedType == JavaType.char && actualType == JavaType.String -> functionCall(
      JavaType.String,
      "charAt",
      listOf(IntConstantNode(node.token, 0)),
      node
    )

    actualType == JavaType.Object -> cast(expectedType, cast(expectedType.objectType, node))
    else -> incompatibleTypes(node, expectedType, actualType)
  }


  private fun primitiveToObjectJavaCast(expectedType: JavaType, node: ExpressionNode, actualType: JavaType): ExpressionNode = when {
    JavaType.Boolean.isSelfOrSuper(expectedType) && actualType == JavaType.boolean
        || JavaType.Integer.isSelfOrSuper(expectedType) && actualType == JavaType.int
        || JavaType.Character.isSelfOrSuper(expectedType) && actualType == JavaType.char
        || JavaType.Long.isSelfOrSuper(expectedType) && actualType == JavaType.long
        || JavaType.Float.isSelfOrSuper(expectedType) && actualType == JavaType.float
        || JavaType.Short.isSelfOrSuper(expectedType) && actualType == JavaType.short
        || JavaType.Byte.isSelfOrSuper(expectedType) && actualType == JavaType.byte
        || JavaType.Double.isSelfOrSuper(expectedType) && actualType == JavaType.double -> functionCall(
      actualType.objectType,
      "valueOf",
      listOf(node),
      node
    )

    else -> incompatibleTypes(node, expectedType, actualType)
  }

  private fun incompatibleTypes(
    node: ExpressionNode,
    expectedType: JavaType,
    actualType: JavaType
  ): ExpressionNode {
    throw MarcelSemanticException(node.token, "Expected expression of type $expectedType but got $actualType")
  }

  internal fun functionCall(
    ownerType: JavaType,
    name: String,
    arguments: List<ExpressionNode>,
    node: ExpressionNode
  ): FunctionCallNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments)
    return FunctionCallNode(
      method, if (method.isStatic) null else node, arguments, node.token,
      // passing dummy to inform code highlight that this is not a fCall from the real marcel source code
      LexToken.DUMMY
    )
  }
}