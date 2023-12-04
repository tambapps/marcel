package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.type.JavaType

class ExtensionJavaMethod private constructor(
  val actualMethod: JavaMethod,
  override val ownerClass: JavaType,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val actualReturnType: JavaType,
) : AbstractMethod() {
  override val isConstructor = false
  // the static is excluded here in purpose so that self is pushed to the stack
  override val isAbstract = false
  override val isDefault = false
  override val isExtension = true

  override val visibility = actualMethod.visibility
  override val isStatic = actualMethod.isStatic
  // needed when computing descriptor/signature
  override val actualParameters = actualMethod.actualParameters

  // could probably do some optimization here (with uses of java reflect API)
  constructor(javaMethod: JavaMethod): this(javaMethod,
    javaMethod.ownerClass, javaMethod.name,
    javaMethod.parameters.takeLast(javaMethod.parameters.size - 1),
    javaMethod.returnType,
    javaMethod.actualReturnType)

  override fun withGenericTypes(types: List<JavaType>): JavaMethod {
    val actualOwnerClass = actualMethod.parameters.first().type.withGenericTypes(types)
    return ExtensionJavaMethod(actualMethod, ownerClass, name,
      actualMethod.parameters.takeLast(actualMethod.parameters.size - 1),
      returnType,
      actualMethod.actualReturnType)
  }
  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
