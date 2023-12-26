package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import kotlin.math.max

/**
 * Trait providing methods to verify if a JavaMethod matches another
 *
 */
interface MethodMatcherTrait {


  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod?

  fun exactMatch(method: JavaMethod, name: String, types: List<JavaTyped>): Boolean {
    return method.name == name && method.parameters.map { it.type } == types.map { it.type }
  }
  fun matches(method: JavaMethod, name: String, types: List<JavaTyped>, strict: Boolean = false): Boolean {
    return method.name == name && matches(method, types, strict)
  }

  // TODO split strict match and match strict=false in separate methods
  fun matches(method: JavaMethod, argumentTypes: List<JavaTyped>, strict: Boolean = false): Boolean {
    if (strict && argumentTypes.size != method.parameters.size
      || !strict && argumentTypes.size > method.parameters.size) return false
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = method.parameters[i].type
      val actualType = argumentTypes[i].type
      if (!methodParameterTypeMatches(expectedType, actualType, strict)) return false
      i++
    }

    // if all remaining parameters have default value, this is a valid function call
    while (i < method.parameters.size) {
      if (!method.parameters[i].hasDefaultValue) return false
      i++
    }
    return i == max(method.parameters.size, argumentTypes.size)
  }

  private fun methodParameterTypeMatches(expectedType: JavaType, actualType: JavaType, strict: Boolean): Boolean {
    return if (expectedType.isInterface && actualType.isLambda) {
      return getInterfaceLambdaMethod(expectedType) != null // lambda parameter matches will be done by lambda handler
    } else if (!strict) expectedType.isAssignableFrom(actualType)
    else expectedType.raw() == actualType.raw()
  }

  fun matchesUnorderedParameters(method: JavaMethod, name: String,
                                         positionalArguments: List<JavaTyped>,
                                         arguments: Collection<MethodParameter>): Boolean {
    if (positionalArguments.isNotEmpty()) {
      if (positionalArguments.size > method.parameters.size || positionalArguments.size + arguments.size > method.parameters.size) return false
      for (i in positionalArguments.indices) {
        if (!method.parameters[i].type.isAssignableFrom(positionalArguments[i].type)) {
          return false
        }
      }
    }
    val methodParameters = method.parameters.subList(positionalArguments.size, method.parameters.size)
    if (method.name != name) return false
    if (arguments.size > methodParameters.size || arguments.any { p -> methodParameters.none { it.name == p.name } }) return false
    for (methodParameter in methodParameters) {
      if (arguments.none { methodParameter.type.isAssignableFrom(it.type) && it.name == methodParameter.name } && methodParameter.defaultValue == null) {
        return false
      }
    }
    return true
  }

}