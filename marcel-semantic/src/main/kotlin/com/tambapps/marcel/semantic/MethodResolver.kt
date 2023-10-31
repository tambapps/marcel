package com.tambapps.marcel.semantic

import com.tambapps.marcel.semantic.ast.ImportNode
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
                    namedArguments: List<Pair<String, ExpressionNode>>, lookupImports: Boolean = false): Pair<JavaMethod, List<ExpressionNode>>? {
    if (namedArguments.isEmpty()) {
      var method = typeResolver.findMethod(ownerType, name, positionalArguments)
      if (method == null && lookupImports) {
        // then search on (static) imports
        method = imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, positionalArguments) }.firstOrNull()
      }
      return method?.let { Pair(it, positionalArguments) }
    }
    val namedMethodParameters = namedArguments.map { MethodParameter(it.second.type, it.first) }
    var method = typeResolver.findMethodByParameters(ownerType, name, positionalArguments, namedMethodParameters)
    if (method == null && lookupImports) {
      // then search on (static) imports
      method = null // TODO resolve with named parameters

    }
    /*
    val method = getMethod(typeResolver)
    return positionalArguments + method.parameters.subList(positionalArguments.size, method.parameters.size).map { parameter: MethodParameter ->
      namedArguments.find { it.name  == parameter.name }?.valueExpression
        ?: parameter.defaultValue
        ?: parameter.type.defaultValueExpression
    }
     */
    return TODO()
  }

}