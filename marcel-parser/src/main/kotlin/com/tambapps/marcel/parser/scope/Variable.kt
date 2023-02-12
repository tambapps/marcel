package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes

sealed interface Variable : AstTypedObject{

  override val type: JavaType
  val name: String
}

class LocalVariable constructor(override var type: JavaType, override var name: String,
                                internal var nbSlots: Int,
                                val index: Int = 0): Variable {

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
  val putCode = if (isStatic) Opcodes.PUTSTATIC else Opcodes.PUTFIELD
}

// for getter/setters
class MethodField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                  private val _getterMethod: JavaMethod?,
                  private val _setterMethod: JavaMethod?,
                  override val access: Int): MarcelField {
  val canGet = _getterMethod != null
  val canSet = _setterMethod != null

  val getterMethod get() = _getterMethod!!
  val setterMethod get() = _setterMethod!!
  val invokeCode =  if (isStatic) Opcodes.INVOKESTATIC
  else if (owner.isInterface) Opcodes.INVOKEINTERFACE
  else Opcodes.INVOKEVIRTUAL

  companion object {
    fun from(owner: JavaType, name: String, getterMethod: JavaMethod?, setterMethod: JavaMethod?): MethodField {
      val type = getterMethod?.returnType ?: setterMethod?.parameters?.first()?.type!!
      val access = getterMethod?.access ?: setterMethod?.access!!
      return MethodField(type, name, owner, getterMethod, setterMethod, access)
    }
  }

}