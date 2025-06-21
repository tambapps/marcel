package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.semantic.type.JavaType

class ExtensionMarcelMethod constructor(
  override val ownerClass: JavaType,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val isMarcelStatic: Boolean,
  val actualMethod: MarcelMethod,
  /**
   * The extended type
   */
  val marcelOwnerClass: JavaType
) : AbstractMethod() {

  override val ownerString: String
    get() = marcelOwnerClass.simpleName

  companion object {

    const val THIS_PARAMETER_NAME = "\$self"

     fun toExtension(originalMethod: MarcelMethod, marcelOwnerClass: JavaType): ExtensionMarcelMethod {
      return if (isInstanceExtensionMethod(originalMethod)) instanceMethodExtension(originalMethod)
      else staticMethodExtension(originalMethod, marcelOwnerClass)
    }

    fun instanceMethodExtension(javaMethod: MarcelMethod): ExtensionMarcelMethod {
      return ExtensionMarcelMethod(
        javaMethod.ownerClass, javaMethod.name,
        javaMethod.parameters.takeLast(javaMethod.parameters.size - 1),
        javaMethod.returnType,
        isMarcelStatic = false,
        javaMethod,
        javaMethod.parameters.first().type
        )
    }

    fun staticMethodExtension(javaMethod: MarcelMethod,  marcelOwnerClass: JavaType): ExtensionMarcelMethod {
      return ExtensionMarcelMethod(
        javaMethod.ownerClass, javaMethod.name,
        javaMethod.parameters,
        javaMethod.returnType,
        isMarcelStatic = true,
        javaMethod,
        marcelOwnerClass
        )
    }

    private fun isInstanceExtensionMethod(originalMethod: MarcelMethod): Boolean {
      val extendedType = originalMethod.ownerClass.globalExtendedType ?: originalMethod.parameters.firstOrNull()?.type

      return extendedType != null && originalMethod.isStatic && originalMethod.parameters.isNotEmpty() && originalMethod.parameters.first().let {
        it.type == extendedType && it.name == THIS_PARAMETER_NAME
      }
    }

  }
  override val isConstructor = false
  // the static is excluded here in purpose so that self is pushed to the stack
  override val isAbstract = false
  override val isDefault = false
  override val isExtension = true
  override val isSynthetic = false
  override val isVarArgs = actualMethod.isVarArgs

  override val visibility = actualMethod.visibility
  override val isStatic = actualMethod.isStatic
  // needed when computing descriptor/signature
  override val actualParameters = actualMethod.actualParameters

  override fun withGenericTypes(types: List<JavaType>): MarcelMethod {
    return ExtensionMarcelMethod(ownerClass, name,
      actualMethod.parameters.takeLast(actualMethod.parameters.size - 1),
      returnType, isMarcelStatic, actualMethod, marcelOwnerClass)
  }
}
