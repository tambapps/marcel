package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.TypedNode
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


  fun matches(name: String, types: List<TypedNode>): Boolean {
    if (parameters.size != types.size) return false
    for (i in parameters.indices) {
      if (!parameters[i].type.isAssignableFrom(types[i].type)) return false
    }
    return this.name == name
  }
}

class ReflectJavaConstructor(constructor: Constructor<*>): JavaMethod {
  override val ownerClass = JavaType(constructor.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = constructor.modifiers
  override val name: String = constructor.name
  override val parameters = constructor.parameters.map { MethodParameter(JavaType(it.type), it.name) }
  override val returnType = JavaType.void // yes, constructor returns void, especially for the descriptor
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val invokeCode = Opcodes.INVOKESPECIAL
  override val isConstructor = true

}
class ReflectJavaMethod(method: Method): JavaMethod {
  override val ownerClass = JavaType(method.declaringClass)

  // see norm of modifiers flag in Modifier class. Seems to have the same norm as OpCodes.ACC_ modifiers
  override val access = method.modifiers
  override val name: String = method.name
  override val parameters = method.parameters.map { MethodParameter(JavaType(it.type), it.name) }
  override val returnType = JavaType(method.returnType)
  override val descriptor = AsmUtils.getDescriptor(parameters, returnType)
  override val isConstructor = false
}