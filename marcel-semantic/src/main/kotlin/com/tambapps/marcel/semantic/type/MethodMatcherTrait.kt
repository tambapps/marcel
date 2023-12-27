package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import kotlin.math.max

/**
 * Trait providing methods to verify if a JavaMethod matches another
 */
interface MethodMatcherTrait {


  /**
   * Get the lambda method of a functional interface or null if ambiguous
   *
   * @param type the interface type
   * @return the lambda method of a functional interface or null if ambiguous
   */
  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod?

  fun exactMatch(method: JavaMethod, name: String, types: List<JavaTyped>): Boolean {
    return method.name == name && method.parameters.map { it.type.raw() } == types.map { it.type.raw() }
  }

  /**
   * Returns whether the method matches the provided method name and argument types. In other words
   * whether the provided method matches the provided name, and the argument types matches the method parameters
   *
   * @param method the JavaMethod
   * @param name the name of the method
   * @param types the argument types
   * @return whether the method matches the provided method name and argument types
   */
  fun matches(method: JavaMethod, name: String, types: List<JavaTyped>): Boolean {
    return method.name == name && matches(method, types)
  }

  /**
   *
   * Returns whether the method matches the provided argument types. In other words
   * whether the provided method could be called with the provided argument types
   *
   * @param method
   * @param argumentTypes
   * @return whether the method matches the provided argument types
   */
  fun matches(method: JavaMethod, argumentTypes: List<JavaTyped>): Boolean {
    if (!method.isVarArgs && argumentTypes.size > method.parameters.size) return false
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = method.parameters[i].type
      val actualType = argumentTypes[i].type
      if (!methodParameterTypeMatches(expectedType, actualType)) return false
      i++
    }

    // if all remaining parameters have default value, this is a valid function call
    while (i < method.parameters.size) {
      if (!method.parameters[i].hasDefaultValue) return false
      i++
    }
    return i == max(method.parameters.size, argumentTypes.size)
  }

  private fun methodParameterTypeMatches(expectedType: JavaType, actualType: JavaType): Boolean {
    return if (expectedType.isInterface && actualType.isLambda)
      getInterfaceLambdaMethod(expectedType) != null // lambda parameter matches will be done by lambda handler
    else expectedType.isAssignableFrom(actualType)
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