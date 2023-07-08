package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.Visibility
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

class MarcelField(override val name: String): Variable {
  val classField: ClassField? get() = getters.firstNotNullOfOrNull { it as? ClassField  }
  private val _getters = mutableSetOf<JavaField>()
  private val _setters = mutableSetOf<JavaField>()
  val getters: Set<JavaField> = _getters
  val setters: Set<JavaField> = _setters

  override val isFinal get() = (getters + setters).all { it.isFinal }
  val isStatic get() = (getters + setters).all { it.isStatic }
  override var alreadySet = false
  override fun isAccessibleFrom(scope: Scope) = (getters + setters).any { it.isAccessibleFrom(scope) }

  override val type get() =  JavaType.commonType((getters + setters).map { it.type })

  constructor(field: JavaField): this(field.name) {
    mergeWith(field)
  }
  fun addGetter(javaField: JavaField) {
    _getters.add(javaField)
  }
  fun addSetter(javaField: JavaField) {
    _setters.add(javaField)
  }

  fun mergeWith(field: JavaField) {
    if (field.isGettable) addGetter(field)
    if (field.isSettable) addSetter(field)
  }

  fun mergeWith(other: MarcelField) {
    _getters.addAll(other._getters)
    _setters.addAll(other._setters)
  }

  fun settableFieldFrom(scope: Scope) = setters.find { it.isAccessibleFrom(scope) }
  fun gettableFieldFrom(scope: Scope) = getters.find { it.isAccessibleFrom(scope) }
}


// can be a java field or a java getter/setter
sealed interface JavaField: Variable {
  val owner: JavaType
  val access: Int
  val visibility: Visibility
  val isStatic: Boolean
    get() = (access and Opcodes.ACC_STATIC) != 0
  override val isFinal: Boolean
    get() = (access and Opcodes.ACC_FINAL) != 0


  val isGettable: Boolean
  val isSettable: Boolean
}

sealed class AbstractField(final override val access: Int): AbstractVariable(), JavaField {
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
  override val isSettable = true
  override val isGettable = true
}

// for getter/setters
open class MethodField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                  private val _getterMethod: JavaMethod?,
                  private val _setterMethod: JavaMethod?,
                              access: Int): AbstractField(access) {

  override val isFinal = false

  override val isGettable = _getterMethod != null
  override val isSettable = _setterMethod != null
  val getterMethod get() = _getterMethod!!
  val setterMethod get() = _setterMethod!!

  companion object {
    fun from(owner: JavaType, name: String, getterMethod: JavaMethod?, setterMethod: JavaMethod?): MethodField {
      val type = getterMethod?.returnType ?: setterMethod?.parameters?.first()?.type!!
      val access = getterMethod?.access ?: setterMethod?.access!!
      return MethodField(type, name, owner, getterMethod, setterMethod, access)
    }
  }
}

class DynamicMethodField(
  type: JavaType,
  name: String,
  owner: JavaType,
  _getterMethod: JavaMethod?,
  _setterMethod: JavaMethod?,
  access: Int
) : MethodField(type, name, owner, _getterMethod, _setterMethod, access)

class ReflectJavaField private constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType,
  access: Int
) : ClassField(type, name, owner, access) {
  constructor(field: Field): this(JavaType.of(field.type), field.name, JavaType.of(field.declaringClass), field.modifiers)
}

// field from binding, for scripts
class BoundField constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType) : AbstractField(Opcodes.ACC_PUBLIC) {

  override val isGettable = true
  override val isSettable = true
    fun withOwner(owner: JavaType) = BoundField(type, name, owner)

}