package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.TypedNode
import org.objectweb.asm.Opcodes

interface JavaMethod {
  val ownerClass: JavaType
  val access: Int
  val name: String
  val parameters: MutableList<MethodParameter>
  val returnType: JavaType
  val descriptor: String
  // TODO generate invokeCode based on access

  val invokeCode: Int
    get() {
      if ((access and Opcodes.ACC_STATIC) != 0) {
        return Opcodes.INVOKESTATIC
      } else {
        TODO("Don't handle this kind of invoke yet")
      }
    }
  fun matches(name: String, types: List<TypedNode>): Boolean {
    if (parameters.size != types.size) return false
    for (i in parameters.indices) {
      if (parameters[i].type != types[i].type) return false
    }
    return this.name == name
  }
}