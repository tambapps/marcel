package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Class allowing to resolve method and method parameters
 */
internal class MethodResolver(
  private val typeResolver: JavaTypeResolver,
  private val nodeCaster: AstNodeCaster,
  private val imports: List<ImportNode>
) {

  fun resolveMethodOrThrow(node: CstNode, ownerType: JavaType, name: String, positionalArguments: List<ExpressionNode>,
                    namedArguments: List<Pair<String, ExpressionNode>>): Pair<JavaMethod, List<ExpressionNode>> {
    return resolveMethod(node, ownerType, name, positionalArguments, namedArguments)
      ?: throw MarcelSemanticException(node.token, "Method $ownerType.$name with parameters ${positionalArguments.map { it.type }} is not defined")
  }

  fun resolveMethod(node: CstNode, ownerType: JavaType, name: String, positionalArguments: List<ExpressionNode>,
                    namedArguments: List<Pair<String, ExpressionNode>>): Pair<JavaMethod, List<ExpressionNode>>? {
    if (namedArguments.isEmpty()) {
      val method = typeResolver.findMethod(ownerType, name, positionalArguments)
      if (method != null) {
        return Pair(method, positionalArguments)
      }

      // handle dynamic object method call
      if (ownerType.implements(JavaType.DynamicObject) && name != JavaMethod.CONSTRUCTOR_NAME) {
        val dynamicInvokeMethod = typeResolver.findMethod(JavaType.DynamicObject, "invokeMethod", listOf(JavaType.String, JavaType.objectArray))!!
        return Pair(dynamicInvokeMethod, listOf(StringConstantNode(name, node),
          ArrayNode(positionalArguments.map { nodeCaster.cast(JavaType.Object, it) }.toMutableList(),
            node, JavaType.objectArray)))
      }
      return null
    }

    val namedMethodParameters = namedArguments.map { MethodParameter(it.second.type, it.first) }
    val method = typeResolver.findMethodByParameters(ownerType, name, positionalArguments, namedMethodParameters)
    if (method != null) {
      val arguments = positionalArguments + method.parameters.subList(positionalArguments.size, method.parameters.size).map { parameter ->
        namedArguments.find { it.first == parameter.name }?.second
          ?: parameter.defaultValue
          ?: parameter.type.getDefaultValueExpression(node.token)
      }
      return Pair(method, arguments)
    }
    return null
  }

  fun resolveMethodFromImports(node: CstNode, name: String, positionalArguments: List<ExpressionNode>,
                               namedArguments: List<Pair<String, ExpressionNode>>): Pair<JavaMethod, List<ExpressionNode>>? {
    for (import in imports) {
      import as? StaticImportNode ?: continue
      val type = typeResolver.of(import.className, emptyList())
      val result = resolveMethod(node, type, name, positionalArguments, namedArguments)
      if (result != null && result.first.isStatic) return result
    }
    return null
  }

}