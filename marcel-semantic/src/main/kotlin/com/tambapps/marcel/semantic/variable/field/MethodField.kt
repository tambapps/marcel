package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.VariableVisitor

/**
 * A Field using a getter and/or a setter
 */
open class MethodField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                                   private val _getterMethod: JavaMethod?,
                                   private val _setterMethod: JavaMethod?,
                                   val isExtension: Boolean): AbstractField() {

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
}
