package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

interface Variable {

  val type: JavaType
  val name: String
}

class LocalVariable(override val type: JavaType, override val name: String): Variable {

  // yup, long and doubles takes 2 variable slots
  val nbSlots = if (type == JavaType.long || type == JavaType.double) 2 else 1
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

}

// can be a java field or a java getter/setter
interface MarcelField: Variable {
  val owner: JavaType
  val getCode: Int
  val access: Int
  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
}
class ClassField(override val type: JavaType, override val name: String, override val owner: JavaType, override val access: Int): MarcelField {
  override val getCode = if (isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD
}

// for getter/setters
class MethodField(override val type: JavaType, override val name: String, val owner: JavaType,
                  val access: Int): Variable