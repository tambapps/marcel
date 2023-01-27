package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

sealed interface Variable : AstTypedObject{

  override val type: JavaType
  val name: String
}

class LocalVariable(override val type: JavaType, override val name: String, var index: Int = 0): Variable {

  // yup, long and doubles takes 2 variable slots
  val nbSlots = if (type == JavaType.long || type == JavaType.double) 2 else 1
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

}

// can be a java field or a java getter/setter
sealed interface MarcelField: Variable {
  val owner: JavaType
  val access: Int
  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
}
class ClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType, override val access: Int): MarcelField {
  val getCode = if (isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD
}

// for getter/setters
class MethodField(override val type: JavaType, override val name: String, override val owner: JavaType,
                  private val _getterName: String?,
                  private val _setterName: String?,
                  override val access: Int): MarcelField {
  val canGet = _getterName != null
  val canSrt = _setterName != null

  val getterName get() = _getterName!!
  val setterName get() = _setterName!!
  val invokeCode =  if (isStatic) Opcodes.INVOKESTATIC
  else if (owner.isInterface) Opcodes.INVOKEINTERFACE
  else Opcodes.INVOKEVIRTUAL

  companion object {
    fun from(owner: JavaType, name: String, getterMethod: JavaMethod?, setterMethod: JavaMethod?): MethodField {
      val type = getterMethod?.returnType ?: setterMethod?.parameters?.first()?.type!!
      val access = getterMethod?.access ?: setterMethod?.access!!
      return MethodField(type, name, owner, getterMethod?.name, setterMethod?.name, access)
    }
  }

}