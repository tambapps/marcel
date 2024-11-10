package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.ExtensionMarcelMethod
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.VariableVisitor

/**
 * A Field using a getter and/or a setter
 */
open class MethodField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                                   private val _getterMethod: MarcelMethod?,
                                   private val _setterMethod: MarcelMethod?,
                                   override val isExtension: Boolean): AbstractField() {
  companion object {

    fun fromGetter(method: MarcelMethod) = MethodField(method.returnType, method.propertyName, method.ownerClass, method, null, method is ExtensionMarcelMethod)
    fun fromSetter(method: MarcelMethod) = MethodField(method.parameters.first().type, method.propertyName, method.ownerClass, null, method, method is ExtensionMarcelMethod)

  }
  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  override val isFinal = false
  override val visibility = computeVisibility()
  override val isStatic = _getterMethod?.isStatic ?: _setterMethod?.isStatic ?: false

  override val isGettable = _getterMethod != null
  override val isSettable = _setterMethod != null

  val getterMethod get() = _getterMethod!!
  val setterMethod get() = _setterMethod!!

  private fun computeVisibility(): Visibility {
    val v1 = _getterMethod?.visibility ?: Visibility.PUBLIC
    val v2 = _setterMethod?.visibility ?: Visibility.PUBLIC
    return if (v1 > v2) v1 else v2
  }

  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access): Boolean {
    return super.isVisibleFrom(javaType, access) && when (access) {
      Variable.Access.GET -> _getterMethod?.visibility?.canAccess(javaType, owner) == true
      Variable.Access.SET -> _setterMethod?.visibility?.canAccess(javaType, owner) == true
      Variable.Access.ANY -> _setterMethod?.visibility?.canAccess(javaType, owner) == true || _getterMethod?.visibility?.canAccess(javaType, owner) == true
    }
  }
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MethodField) return false
    if (!super.equals(other)) return false

    if (_getterMethod != other._getterMethod) return false
    if (_setterMethod != other._setterMethod) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + (_getterMethod?.hashCode() ?: 0)
    result = 31 * result + (_setterMethod?.hashCode() ?: 0)
    return result
  }
}
