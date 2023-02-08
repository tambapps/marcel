package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstTypedObject
import org.objectweb.asm.Opcodes
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

interface JavaMethod {

  companion object {
    const val CONSTRUCTOR_NAME = "<init>"
  }

  val ownerClass: JavaType
  val access: Int
  val name: String
  val parameters: List<MethodParameter>
  val returnType: JavaType
  val descriptor: String
  val signature: String?
    get() {
      if (parameters.none { it.type.hasGenericTypes } && !returnType.hasGenericTypes) return null
      val builder = StringBuilder()
      parameters.joinTo(buffer = builder, separator = "", transform = { it.type.signature ?: it.type.descriptor }, prefix = "(", postfix = ")")
      builder.append(returnType.signature ?: returnType.descriptor)
      return builder.toString()
    }
  val isDefault: Boolean
  val isAbstract: Boolean

  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
  val isConstructor: Boolean
  val isInline: Boolean get() = false
  val invokeCode: Int
    get() = if (isStatic) Opcodes.INVOKESTATIC
    else if (ownerClass.isInterface) Opcodes.INVOKEINTERFACE
    else Opcodes.INVOKEVIRTUAL


  fun matches(typeResolver: AstNodeTypeResolver, name: String, types: List<AstTypedObject>): Boolean {
    return this.name == name && matches(typeResolver, types)
  }

  fun matches(typeResolver: AstNodeTypeResolver, types: List<AstTypedObject>): Boolean {
    if (parameters.size != types.size) return false
    for (i in parameters.indices) {
      val expectedType = parameters[i].type
      val actualType = types[i].type
      if (!matches(typeResolver, expectedType, actualType)) return false
    }
    return true
  }

  private fun matches(typeResolver: AstNodeTypeResolver, expectedType: JavaType, actualType: JavaType): Boolean {
    return if (expectedType.isInterface && actualType.isLambda) {
      val declaredMethods = typeResolver.getDeclaredMethods(expectedType)
        .filter { it.isAbstract }
      if (declaredMethods.size != 1) return false
      val interfaceMethod = declaredMethods.first()
      val lambdaMethod = typeResolver.getDeclaredMethods(actualType).first { it.isAbstract }
      return interfaceMethod.parameters.size == lambdaMethod.parameters.size // TODO don't know if there's a better way for that
    //return interfaceMethod.matches(typeResolver, lambdaMethod.parameters)
    } else expectedType.isAssignableFrom(actualType)
  }
}

class ReflectJavaConstructor(constructor: Constructor<*>): JavaMethod {
  override val ownerClass = JavaType.of(constructor.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = constructor.modifiers
  override val name: String = JavaMethod.CONSTRUCTOR_NAME
  override val parameters = constructor.parameters.map { MethodParameter(JavaType.of(it.type), it.name) }
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val invokeCode = Opcodes.INVOKESPECIAL
  override val isConstructor = true
  override val isDefault = false
  override val isAbstract = false

  override fun toString(): String {
    return "${ownerClass.className}(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class ExtensionJavaMethod(
  override val ownerClass: JavaType,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
  override val descriptor: String,
) : JavaMethod {
  override val isConstructor = false
  // the static is excluded here in purpose so that self is pushed to the stack
  override val access = Opcodes.ACC_PUBLIC
  override val invokeCode = Opcodes.INVOKESTATIC
  override val isAbstract = false
  override val isDefault = false

  constructor(method: Method): this(JavaType.of(method.declaringClass), method.name,
    method.parameters.takeLast(method.parameters.size - 1).map { MethodParameter(JavaType.of(it.type), it.name) },
    JavaType.of(method.returnType), AsmUtils.getDescriptor(method))

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class ReflectJavaMethod constructor(method: Method, fromType: JavaType?): JavaMethod {

  constructor(method: Method): this(method, null)

  override val ownerClass = JavaType.of(method.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = method.modifiers
  override val name: String = method.name
  override val parameters = method.parameters.map { MethodParameter(methodParameterType(fromType, it), it.name) }
  override val returnType = JavaType.of(method.returnType)
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val isConstructor = false
  override val isAbstract = (method.modifiers and Modifier.ABSTRACT) != 0
  override val isDefault = method.isDefault

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }

  companion object {
    fun methodParameterType(javaType: JavaType?, methodParameter: Parameter): JavaType {
      val rawType = JavaType.of(methodParameter.type)
      if (javaType == null || javaType.genericTypes.isEmpty()) return rawType
      val parameterizedType = methodParameter.parameterizedType
      val parameterNames = javaType.genericParameterNames
      if (parameterizedType is ParameterizedType) {

        val genericTypes = parameterizedType.actualTypeArguments.map {
          if (it is WildcardType) {
            var index = parameterNames.indexOf(it.upperBounds.first().typeName)
            if (index < 0) index = parameterNames.indexOf(it.lowerBounds.first().typeName)
            return@map javaType.genericTypes.getOrNull(index) ?: JavaType.Object
          } else {
            TODO("Sounds difficult to implement")
          }
        }
        return rawType.withGenericTypes(genericTypes)
      } else if (parameterizedType is TypeVariable<*>) {
        val index = parameterNames.indexOf(parameterizedType.name)
        return javaType.genericTypes.getOrNull(index) ?: rawType
      } else {
        return rawType
      }
    }
  }
}