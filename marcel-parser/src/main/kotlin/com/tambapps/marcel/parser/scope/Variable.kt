package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.Visibility
import marcel.lang.Binding
import org.objectweb.asm.Opcodes
import java.lang.reflect.Field

sealed interface Variable : AstTypedObject{
  fun isAccessibleFrom(scope: Scope): Boolean

    override val type: JavaType
  val name: String
  val isFinal: Boolean
  var alreadySet: Boolean
}

sealed class AbstractVariable: Variable {
  override var alreadySet = false

}

class LocalVariable constructor(override var type: JavaType, override var name: String,
                                internal val nbSlots: Int,
                                val index: Int = 0,
                                override var isFinal: Boolean): AbstractVariable() {
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

  fun reset(type: JavaType, name: String, isSettable: Boolean) {
    this.type = type
    this.name = name
    this.isFinal = isSettable
    alreadySet = false
  }

  override fun isAccessibleFrom(scope: Scope): Boolean {
    return true
  }
}

// can be a java field or a java getter/setter
sealed interface MarcelField: Variable {
  val owner: JavaType
  val access: Int
  val visibility: Visibility
  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
  override val isFinal: Boolean
    get() = (access and Opcodes.ACC_FINAL) != 0


}

sealed class AbstractField(final override val access: Int): AbstractVariable(), MarcelField {
  override var alreadySet = false

  override val visibility = Visibility.fromAccess(access)

  override fun toString(): String {
    return "$type $name"
  }

  override fun isAccessibleFrom(scope: Scope): Boolean {
    return visibility.canAccess(scope.classType, owner)
  }
}

open class ClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType, access: Int): AbstractField(access) {
  val getCode = if (isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD
  val putCode = if (isStatic) Opcodes.PUTSTATIC else Opcodes.PUTFIELD
}

// for getter/setters
class MethodField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                  private val _getterMethod: JavaMethod?,
                  private val _setterMethod: JavaMethod?,
                              access: Int): AbstractField(access) {
  val canGet = _getterMethod != null
  val canSet = _setterMethod != null
  override val isFinal = false

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

class ReflectMarcelField private constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType,
  access: Int
) : AbstractField(access) {
  constructor(field: Field): this(JavaType.of(field.type), field.name, JavaType.of(field.declaringClass), field.modifiers)
}

// field from binding, for scripts
class BoundField constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType) : AbstractField(Opcodes.ACC_PUBLIC) {

    fun withOwner(owner: JavaType) = BoundField(type, name, owner)

}