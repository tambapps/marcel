package com.tambapps.marcel.semantic.ast.cast

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.DynamicObject
import marcel.lang.runtime.BytecodeHelper

/**
 * Class transforming nodes if necessary in order to cast them.
 */
class AstNodeCaster(
  private val typeResolver: JavaTypeResolver
) {

  /**
   * Cast the provided node (if necessary) so that it fits the expected type.
   * Throws a MarcelSemanticException in case of casting failure
   */
  fun cast(expectedType: JavaType, node: ExpressionNode): ExpressionNode {
    val actualType = node.type
    if (expectedType.isExtendedOrImplementedBy(actualType)) return node
    // Object to primitive
    else if (!expectedType.isAssignableFrom(actualType)) throw MarcelSemanticException(node.token, "Expected expression of type $expectedType but got $actualType")
    else if (expectedType == JavaType.boolean && actualType == JavaType.Boolean) functionCall(JavaType.Boolean, "booleanValue", emptyList(), node)
    else if (expectedType == JavaType.int && actualType == JavaType.Integer) functionCall(JavaType.Integer, "intValue", emptyList(), node)
    else if (expectedType == JavaType.char && actualType == JavaType.Character) functionCall(JavaType.Character, "charValue", emptyList(), node)
    else if (expectedType == JavaType.long && actualType == JavaType.Long) functionCall(JavaType.Long, "longValue", emptyList(), node)
    else if (expectedType == JavaType.float && actualType == JavaType.Float) functionCall(JavaType.Float, "floatValue", emptyList(), node)
    else if (expectedType == JavaType.double && actualType == JavaType.Double) functionCall(JavaType.Double, "doubleValue", emptyList(), node)
    // TODO do byte to Byte, short to Short
    // primitive to Object
    else if (expectedType == JavaType.Boolean && actualType == JavaType.boolean
      || expectedType == JavaType.Integer && actualType == JavaType.int
      || expectedType == JavaType.Character && actualType == JavaType.char
      || expectedType == JavaType.Long && actualType == JavaType.long
      || expectedType == JavaType.Float && actualType == JavaType.float
      || expectedType == JavaType.Double && actualType == JavaType.double) functionCall(expectedType, "valueOf", listOf(node), node)
    // lists
    else if (JavaType.intList.isAssignableFrom(expectedType) && actualType == JavaType.intArray) functionCall(JavaType.intListImpl, "wrap", listOf(node), node)
    else if (JavaType.longList.isAssignableFrom(expectedType) && actualType == JavaType.longArray) functionCall(JavaType.longListImpl, "wrap", listOf(node), node)
    else if (JavaType.floatList.isAssignableFrom(expectedType) && actualType == JavaType.floatArray) functionCall(JavaType.floatListImpl, "wrap", listOf(node), node)
    else if (JavaType.doubleList.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray) functionCall(JavaType.doubleListImpl, "wrap", listOf(node), node)
    else if (JavaType.charList.isAssignableFrom(expectedType) && actualType == JavaType.charArray) functionCall(JavaType.charListImpl, "wrap", listOf(node), node)
    else if (List::class.javaType.isAssignableFrom(expectedType) && actualType.isArray) functionCall(BytecodeHelper::class.javaType, "createList", listOf(node), node)
    // sets
    else if (JavaType.intSet.isAssignableFrom(expectedType) && actualType == JavaType.intArray
      || JavaType.longSet.isAssignableFrom(expectedType) && actualType == JavaType.longArray
      || JavaType.floatSet.isAssignableFrom(expectedType) && actualType == JavaType.floatArray
      || JavaType.doubleSet.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray
      || JavaType.characterSet.isAssignableFrom(expectedType) && actualType == JavaType.charArray
      || Set::class.javaType.isAssignableFrom(expectedType) && actualType.isArray) functionCall(BytecodeHelper::class.javaType, "createSet", listOf(node), node)
    else if (expectedType == JavaType.DynamicObject) {
      functionCall(DynamicObject::class.javaType, "of", listOf(cast(JavaType.Object, node)), node)
      return functionCall(DynamicObject::class.javaType, "of", listOf(
        cast(JavaType.Object, node) // to handle primitives
      ), node)
    }

    TODO("JavaCast at the end (create a node for that")
  }


  private fun functionCall(ownerType: JavaType, name: String, arguments: List<ExpressionNode>, node: ExpressionNode): FunctionCallNode {
    val method = typeResolver.findMethodOrThrow(ownerType, name, arguments)
    return FunctionCallNode(method, if (method.isStatic) null else node, null, emptyList(), node.token)
  }
}