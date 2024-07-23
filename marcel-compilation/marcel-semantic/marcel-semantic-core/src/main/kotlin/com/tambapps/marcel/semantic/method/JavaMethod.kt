package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaArrayType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped
import com.tambapps.marcel.semantic.variable.LocalVariable

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
  // to handle extension methods when writing them
  val actualParameters: List<MethodParameter> get() = parameters
  val isAsync get() = asyncReturnType != null
  val asyncReturnType: JavaType? get() = null // only use when isAsync is true

  val returnType: JavaType
  val actualReturnType: JavaType
  val isDefault: Boolean // for interface
  val isAbstract: Boolean

  val isStatic: Boolean
  val isExtension: Boolean get() = false
  // extension field/methods are not considered static in marcel
  val isMarcelStatic: Boolean get() = isStatic && !isExtension

  val isConstructor: Boolean
  val isInline: Boolean get() = false
  val isVarArgs: Boolean
  val varArgsType: JavaArrayType
    get() = parameters.last().type.asArrayType
  val varArgType: JavaType
    get() = varArgsType.elementsType

  fun isVisibleFrom(type: JavaType): Boolean {
    return visibility.canAccess(type, ownerClass)
  }

  fun parameterMatches(other: JavaMethod): Boolean {
    if (parameters.size != other.parameters.size) return false
    for (i in parameters.indices) if (parameters[i].type.raw() != other.parameters[i].type.raw()) return false
    return true
  }

  /**
   * Generate a local variable corresponding to the provided method parameter, initializing correctly the index of the
   * local variable based on previous method parameters index and nbSlots if any
   *
   * @param parameter the method parameter (should be a parameter of this method)
   * @return a local variable corresponding to the method parameter
   */
  fun toLocalVariable(parameter: MethodParameter): LocalVariable {
    var index = if (isStatic) 0 else 1
    var i = 0
    while (i < parameters.size && parameters[i] != parameter) {
      index += parameters[i++].type.nbSlots
    }
    return LocalVariable(parameter.type, parameter.name, parameter.type.nbSlots, index, parameter.isFinal)
  }

  /**
   * Returns whether a method matches another, in terms of unique signatures.
   *  if a method matches the other, it means that these two methods can't be declared in a same class
   *  as they would conflict with one another
   *
   * @param other the other method
   * @return whether a method matches another
   */
  fun matches(other: JavaMethod): Boolean {
    if (name != other.name) return false
    if (!parameterMatches(other)) return false
    return true
  }

  fun parametersAssignableTo(other: JavaMethod): Boolean {
    if (parameters.size != other.parameters.size) return false
    for (i in parameters.indices) if (!other.parameters[i].type.isAssignableFrom(parameters[i].type)) return false
    return true
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
