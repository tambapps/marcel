package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped
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
  fun getInterfaceLambdaMethod(type: JavaType): MarcelMethod?

  fun strictMatch(method: MarcelMethod, name: String, types: List<JavaTyped>): Boolean {
    return method.name == name && method.parameters.map { it.type.raw() } == types.map { it.type.raw() }
  }

  /**
   * Returns whether the method matches the provided method name and argument types. In other words
   * whether the provided method matches the provided name, and the argument types matches the method parameters
   *
   * @param method the [MarcelMethod]
   * @param name the name of the method
   * @param argumentTypes the argument types
   * @return whether the method matches the provided method name and argument types
   */
  fun matches(method: MarcelMethod, name: String, argumentTypes: List<JavaTyped>): Boolean {
    return method.name == name && matches(method, argumentTypes)
  }

  /**
   * Returns whether the method matches the provided argument types. In other words
   * whether the provided method could be called with the provided argument types
   *
   * @param method the [MarcelMethod]
   * @param argumentTypes
   * @return whether the method matches the provided argument types
   */
  fun matches(method: MarcelMethod, argumentTypes: List<JavaTyped>): Boolean {
    return matchesMethod(method, argumentTypes)
        || method.isVarArgs && matchesVarArgsMethod(method, argumentTypes)
  }

  /**
   * Returns whether the method matches the provided argument types. In other words
   * whether the provided method could be called with the provided argument types.
   * VarArgs methods are **not** handled by this method
   *
   * @param method the [MarcelMethod]
   * @param argumentTypes
   * @return whether the method matches the provided argument types
   */
  fun matchesMethod(method: MarcelMethod, argumentTypes: List<JavaTyped>): Boolean {
    if (argumentTypes.size > method.parameters.size) return false
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

  private fun matchesVarArgsMethod(method: MarcelMethod, argumentTypes: List<JavaTyped>): Boolean {
    val varArgType = method.varArgType
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = if (i < method.parameters.lastIndex) method.parameters[i].type else varArgType
      val actualType = argumentTypes[i].type
      if (!methodParameterTypeMatches(expectedType, actualType)) return false
      i++
    }
    return i >= method.parameters.size - 1 // - 1 because we could specify empty array for vararg
  }

  private fun methodParameterTypeMatches(expectedType: JavaType, actualType: JavaType): Boolean {
    return if (expectedType.isInterface && actualType.isLambda)
      getInterfaceLambdaMethod(expectedType) != null // lambda parameter matches will be done by lambda handler
    else expectedType.isAssignableFrom(actualType) || JavaType.isListConvertable(expectedType, actualType)  || JavaType.isSetConvertable(expectedType, actualType)
  }

  fun matchesUnorderedParameters(method: MarcelMethod, name: String,
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