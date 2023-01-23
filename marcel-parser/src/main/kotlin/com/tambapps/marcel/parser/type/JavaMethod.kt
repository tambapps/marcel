package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstTypedObject
import org.objectweb.asm.Opcodes
import java.lang.reflect.Constructor
import java.lang.reflect.Method

interface JavaMethod {

  companion object {
    val CONSTRUCTOR_NAME = "<init>"
  }

  val ownerClass: JavaType
  val access: Int
  val name: String
  val parameters: List<MethodParameter>
  val returnType: JavaType
  val descriptor: String

  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
  val isConstructor: Boolean
  val invokeCode: Int
    get() = if (isStatic) Opcodes.INVOKESTATIC
    else if (ownerClass.isInterface) Opcodes.INVOKEINTERFACE
    else Opcodes.INVOKEVIRTUAL


  fun matches(name: String, types: List<AstTypedObject>): Boolean {
    if (parameters.size != types.size) return false
    for (i in parameters.indices) {
      val expectedType = parameters[i].type
      val actualType = types[i].type
      if (!expectedType.isAssignableFrom(actualType)) return false
    }
    return this.name == name
  }
}

class ReflectJavaConstructor(constructor: Constructor<*>): JavaMethod {
  override val ownerClass = JavaType.of(constructor.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = constructor.modifiers
  override val name: String = constructor.name
  override val parameters = constructor.parameters.map { MethodParameter(JavaType.of(it.type), it.name) }
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val invokeCode = Opcodes.INVOKESPECIAL
  override val isConstructor = true

  override fun toString(): String {
    return "${ownerClass.className}(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class SimpleJavaMethod(
  override val ownerClass: JavaType,
  override val access: Int,
  override val name: String,
  override val parameters: List<MethodParameter>,
  override val returnType: JavaType,
) : JavaMethod {

  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)

  constructor(ownerClass: Class<*>,
              access: Int,
              name: String,
              parameters: List<Class<*>>,
              returnType: Class<*>): this(JavaType.of(ownerClass), access, name,
    parameters.map { MethodParameter(JavaType.of(it), it.name) }, JavaType.of(returnType))
  override val isConstructor = false

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}
class ReflectJavaMethod(method: Method): JavaMethod {
  override val ownerClass = JavaType.of(method.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = method.modifiers
  override val name: String = method.name
  override val parameters = method.parameters.map { MethodParameter(JavaType.of(it.type), it.name) }
  override val returnType = JavaType.of(method.returnType)
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val isConstructor = false

  override fun toString(): String {
    return "$ownerClass.$name(" + parameters.joinToString(separator = ", ", transform = { "${it.type} ${it.name}"}) + ") " + returnType
  }
}