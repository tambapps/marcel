package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.Variable
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor

class MarcelArrayLengthField(override val owner: JavaType, override val name: String): MarcelField {

  override val type = JavaType.int
  override val isFinal = false
  override val visibility = Visibility.PUBLIC
  override val isStatic = false
  override val isGettable = true
  override val isSettable = false


  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)
  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access) = true
}
