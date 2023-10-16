package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.JavaTyped

interface JavaMethod: JavaTyped {

  companion object {
    const val CONSTRUCTOR_NAME = "<init>"
    const val STATIC_INITIALIZATION_BLOCK = "<clinit>"

    fun defaultParameterMethodName(method: JavaMethod, parameter: MethodParameter): String {
      return defaultParameterMethodName(method.name, parameter.name)
    }
    fun defaultParameterMethodName(methodName: String, parameterName: String): String {
      return "_" + methodName + "_" + parameterName + "_defaultValue"
    }
  }

  override val type: JavaType
    get() = actualReturnType
  val ownerClass: JavaType
  val visibility: Visibility
  val name: String
  val parameters: List<MethodParameter>
  // to handle extension methods when writting them
  val actualParameters: List<MethodParameter> get() = parameters
  val returnType: JavaType
  // for generic methods TODO might need to be removed
  val actualReturnType: JavaType
  val isDefault: Boolean // for interface
  val isAbstract: Boolean

  val isStatic: Boolean
  val isConstructor: Boolean
  val isInline: Boolean get() = false

  fun isAccessibleFrom(type: JavaType): Boolean {
    return visibility.canAccess(type, ownerClass)
  }

  fun parametersAssignableTo(other: JavaMethod): Boolean {
    if (parameters.size != other.parameters.size) return false
    for (i in parameters.indices) if (!other.parameters[i].type.isAssignableFrom(parameters[i].type)) return false
    return true
  }

  fun parameterMatches(other: JavaMethod): Boolean {
    if (parameters.size != other.parameters.size) return false
    for (i in parameters.indices) if (parameters[i].type != other.parameters[i].type) return false
    return true
  }

  fun matches(other: JavaMethod): Boolean {
    if (name != other.name) return false
    if (!parameterMatches(other)) return false
    if (returnType != other.returnType) return false
    return true
  }
  fun matchesUnorderedParameters(typeResolver: JavaTypeResolver, name: String,
                                 positionalArguments: List<JavaTyped>,
                                 arguments: Collection<MethodParameter>): Boolean {
    if (positionalArguments.isNotEmpty()) {
      if (positionalArguments.size > this.parameters.size || positionalArguments.size + arguments.size > this.parameters.size) return false
      for (i in positionalArguments.indices) {
        if (!this.parameters[i].type.isAssignableFrom(positionalArguments[i].type)) {
          return false
        }
      }
    }
    val methodParameters = this.parameters.subList(positionalArguments.size, this.parameters.size)
    if (this.name != name) return false
    if (arguments.size > methodParameters.size || arguments.any { p -> methodParameters.none { it.name == p.name } }) return false
    for (methodParameter in methodParameters) {
      if (arguments.none { methodParameter.type.isAssignableFrom(it.type) && it.name == methodParameter.name } && methodParameter.defaultValue == null) {
        return false
      }
    }
    return true
  }

  fun matches(typeResolver: JavaTypeResolver, name: String, types: List<JavaTyped>): Boolean {
    return this.name == name && matches(typeResolver, types)
  }

  fun matches(typeResolver: JavaTypeResolver, argumentTypes: List<JavaTyped>): Boolean {
    if (argumentTypes.size > parameters.size) return false
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = parameters[i].type
      val actualType = argumentTypes[i].type
      if (!matches(typeResolver, expectedType, actualType)) return false
      i++
    }

    // if all remaining parameters have default value, this is a valid function call
    while (i < parameters.size) {
      if (!parameters[i].hasDefaultValue) return false
      i++
    }
    return true
  }

  fun exactMatch(name: String, types: List<JavaTyped>): Boolean {
    return this.name == name && this.parameters.map { it.type } == types.map { it.type }
  }

  private fun matches(typeResolver: JavaTypeResolver, expectedType: JavaType, actualType: JavaType): Boolean {
    return if (expectedType.isInterface && actualType.isLambda) {
      val declaredMethods = typeResolver.getDeclaredMethods(expectedType)
        .filter { it.isAbstract }
      if (declaredMethods.size != 1) return false
      val interfaceMethod = declaredMethods.first()
      val lambdaMethod = typeResolver.getInterfaceLambdaMethod(actualType)
      return interfaceMethod.parameters.size == lambdaMethod.parameters.size // there's probably if there's a better way for that
    //return interfaceMethod.matches(typeResolver, lambdaMethod.parameters)
    } else expectedType.isAssignableFrom(actualType)
  }

  fun withGenericTypes(types: List<JavaType>): JavaMethod {
    // this is especially for ExtensionMethod, which don't have generic actual parameters since the type was gotten
    // from the first method's parameter
    return this
  }

  val isGetter get() = name.startsWith("get") && name.getOrNull(3)?.isUpperCase() == true && parameters.isEmpty()
  val isSetter get() = name.startsWith("set") && name.getOrNull(3)?.isUpperCase() == true && parameters.size == 1
  val propertyName: String get() = name[3].lowercase() + name.substring(4)
}
