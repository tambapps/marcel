package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.type.JavaType

class ExtensionJavaMethod  constructor(
  val actualMethod: MarcelMethod,
  override val ownerClass: JavaType,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val isMarcelStatic: Boolean
) : AbstractMethod() {

  companion object {

    const val THIS_PARAMETER_NAME = "self"

    fun toExtension(originalMethod: MarcelMethod, extendedType: JavaType = originalMethod.ownerClass.extendedType!!): ExtensionJavaMethod {
      return if (isInstanceExtensionMethod(originalMethod, extendedType)) instanceMethodExtension(originalMethod)
      else staticMethodExtension(originalMethod)
    }

    private fun isInstanceExtensionMethod(originalMethod: MarcelMethod, extendedType: JavaType = originalMethod.ownerClass.extendedType!!): Boolean {
      return originalMethod.isStatic && originalMethod.parameters.isNotEmpty() && originalMethod.parameters.first().let {
        it.type == extendedType
      }
    }

    private fun instanceMethodExtension(javaMethod: MarcelMethod): ExtensionJavaMethod {
      return ExtensionJavaMethod(
        javaMethod,
        javaMethod.ownerClass, javaMethod.name,
        javaMethod.parameters.takeLast(javaMethod.parameters.size - 1),
        javaMethod.returnType,
        isMarcelStatic = false
      )
    }

    private fun staticMethodExtension(javaMethod: MarcelMethod): ExtensionJavaMethod {
      return ExtensionJavaMethod(
        javaMethod,
        javaMethod.ownerClass, javaMethod.name,
        javaMethod.parameters,
        javaMethod.returnType,
        isMarcelStatic = true
      )
    }
  }
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

  override fun withGenericTypes(types: List<JavaType>): MarcelMethod {
    return ExtensionJavaMethod(actualMethod, ownerClass, name,
      actualMethod.parameters.takeLast(actualMethod.parameters.size - 1),
      returnType, isMarcelStatic)
  }
}
