package com.tambapps.marcel.semantic

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Class allowing to resolve method and method parameters
 */
internal class MethodResolver(
  private val typeResolver: JavaTypeResolver,
  // TODO handle better imports
  private val imports: List<ImportNode>
) {


  // TODO handle delegatedObject
  //    then dynamic object
  fun resolveMethod(ownerType: JavaType, name: String, positionalArguments: List<ExpressionNode>,
                    namedArguments: List<Pair<String, ExpressionNode>>): Pair<JavaMethod, List<ExpressionNode>>? {
    if (namedArguments.isEmpty()) {
      val method = typeResolver.findMethod(ownerType, name, positionalArguments)
      return method?.let { Pair(it, positionalArguments) }
    }
    val namedMethodParameters = namedArguments.map { MethodParameter(it.second.type, it.first) }
    val method = typeResolver.findMethodByParameters(ownerType, name, positionalArguments, namedMethodParameters)
    if (method != null) {
      val arguments = positionalArguments + method.parameters.subList(positionalArguments.size, method.parameters.size).map { parameter ->
        namedArguments.find { it.first == parameter.name }?.second
          ?: parameter.defaultValue
          ?: parameter.type.getDefaultValueExpression(namedArguments.first().second.token)
      }
      return Pair(method, arguments)
    }
    return null
  }

  fun resolveMethodFromImports(name: String, positionalArguments: List<ExpressionNode>,
                               namedArguments: List<Pair<String, ExpressionNode>>): Pair<JavaMethod, List<ExpressionNode>>? {
    for (import in imports) {
      import as? StaticImportNode ?: continue
      val type = typeResolver.of(import.className, emptyList())
      val result = resolveMethod(type, name, positionalArguments, namedArguments)
      if (result != null) return result
    }
    return null
  }

}