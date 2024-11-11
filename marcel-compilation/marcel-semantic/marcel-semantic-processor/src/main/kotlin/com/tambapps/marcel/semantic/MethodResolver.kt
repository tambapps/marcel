package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.getDefaultValueExpression
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

/**
 * Class allowing to resolve method and method parameters
 */
class MethodResolver(
  private val symbolResolver: MarcelSymbolResolver,
  private val nodeCaster: AstNodeCaster,
) {

  fun resolveConstructorCallOrThrow(
    node: CstNode, ownerType: JavaType, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>
  ) = resolveConstructorCall(node, ownerType, positionalArguments, namedArguments)
    ?: throw MarcelSemanticException(
  node.token,
  methodResolveErrorMessage(positionalArguments, namedArguments, ownerType, MarcelMethod.CONSTRUCTOR_NAME)
  )


  fun resolveConstructorCall(
    node: CstNode, ownerType: JavaType, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>
  ) = resolveMethod(node, ownerType, MarcelMethod.CONSTRUCTOR_NAME, positionalArguments, namedArguments)

  fun resolveMethod(
    node: CstNode, ownerType: JavaType, name: String, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>
  ): Pair<MarcelMethod, List<ExpressionNode>>? {
    if (namedArguments.isEmpty()) {
      val method = symbolResolver.findMethod(ownerType, name, positionalArguments)
      if (method != null) {
        return Pair(method, completedArguments(node, method, positionalArguments, namedArguments))
      }

      // handle dynamic object method call without named parameters
      if (ownerType.implements(JavaType.DynamicObject) && name != MarcelMethod.CONSTRUCTOR_NAME) {
        val dynamicInvokeMethod = symbolResolver.findMethod(
          JavaType.DynamicObject,
          "invokeMethod",
          listOf(JavaType.String, JavaType.Map, JavaType.objectArray)
        )!!
        return Pair(
          dynamicInvokeMethod, listOf(
            StringConstantNode(name, node),
            MapNode(emptyList(), node), // no named parameters
            ArrayNode(
              positionalArguments.map { nodeCaster.cast(JavaType.Object, it) }.toMutableList(),
              node, JavaType.objectArray
            )
          )
        )
      }
      return null
    }

    val namedMethodParameters = namedArguments.map { MethodParameter(it.second.type, it.first) }
    val method = symbolResolver.findMethodByParameters(ownerType, name, positionalArguments, namedMethodParameters)
    if (method != null) {
      return Pair(method, completedArguments(node, method, positionalArguments, namedArguments))
    }

    // handle dynamic object method call with named parameters
    if (ownerType.implements(JavaType.DynamicObject) && name != MarcelMethod.CONSTRUCTOR_NAME) {
      val dynamicInvokeMethod = symbolResolver.findMethod(
        JavaType.DynamicObject,
        "invokeMethod",
        listOf(JavaType.String, JavaType.Map, JavaType.objectArray)
      )!!
      return Pair(
        dynamicInvokeMethod, listOf(
          StringConstantNode(name, node),
          MapNode(namedArguments.map { Pair(StringConstantNode(it.first, node), nodeCaster.cast(JavaType.Object, it.second)) }, node),
          ArrayNode(
            positionalArguments.map { nodeCaster.cast(JavaType.Object, it) }.toMutableList(),
            node, JavaType.objectArray
          )
        )
      )
    }
    return null
  }

  fun resolveMethodFromImports(
    node: CstNode, name: String, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>, importResolver: ImportResolver
  ): Pair<MarcelMethod, List<ExpressionNode>>? {

    val ownerType = importResolver.resolveMemberOwnerType(name) ?: return null
    val result = resolveMethod(node, ownerType, name, positionalArguments, namedArguments)
    if (result != null && result.first.isStatic) return result
    else return null
  }

  // complete the arguments if necessary by looking on the method parameters default value and/or named parameters
  private fun completedArguments(
    node: CstNode, method: MarcelMethod, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>
  ): List<ExpressionNode> {
    return if (positionalArguments.size >= method.parameters.size || method.isVarArgs) positionalArguments
    else positionalArguments + method.parameters.subList(positionalArguments.size, method.parameters.size)
      .map { parameter ->
        namedArguments.find { it.first == parameter.name }?.second
          ?: parameter.defaultValue
          ?: parameter.type.getDefaultValueExpression(node.token)
      }
  }

  companion object {
    internal fun methodResolveErrorMessage(
      positionalArguments: List<ExpressionNode>,
      namedArguments: List<Pair<String, ExpressionNode>>,
      ownerType: JavaType,
      name: String
    ): String {
      val allParametersString = mutableListOf<String>()
      positionalArguments.forEach { allParametersString.add(it.type.simpleName) }
      namedArguments.forEach { allParametersString.add("${it.first}: ${it.second.type.simpleName}") }

      val displayedName = if (name == MarcelMethod.CONSTRUCTOR_NAME) "Constructor $ownerType"
      else "Method $ownerType.$name"

      return allParametersString.joinToString(
        separator = ", ",
        prefix = "$displayedName(", postfix = ") is not defined"
      )
    }
  }
}