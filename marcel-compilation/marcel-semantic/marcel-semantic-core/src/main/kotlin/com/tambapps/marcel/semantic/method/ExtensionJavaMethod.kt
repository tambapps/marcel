package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.type.JavaType

class ExtensionJavaMethod private constructor(
  val actualMethod: MarcelMethod,
  override val ownerClass: JavaType,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
) : AbstractMethod() {
  override val isConstructor = false
  // the static is excluded here in purpose so that self is pushed to the stack
  override val isAbstract = false
  override val isDefault = false
  override val isExtension = true
  override val isVarArgs = actualMethod.isVarArgs

  override val visibility = actualMethod.visibility
  override val isStatic = actualMethod.isStatic
  // needed when computing descriptor/signature
  override val actualParameters = actualMethod.actualParameters

  // could probably do some optimization here (with uses of java reflect API)
  constructor(javaMethod: MarcelMethod): this(javaMethod,
    javaMethod.ownerClass, javaMethod.name,
    javaMethod.parameters.takeLast(javaMethod.parameters.size - 1),
    javaMethod.returnType)

  override fun withGenericTypes(types: List<JavaType>): MarcelMethod {
    return ExtensionJavaMethod(actualMethod, ownerClass, name,
      actualMethod.parameters.takeLast(actualMethod.parameters.size - 1),
      returnType)
  }

}
