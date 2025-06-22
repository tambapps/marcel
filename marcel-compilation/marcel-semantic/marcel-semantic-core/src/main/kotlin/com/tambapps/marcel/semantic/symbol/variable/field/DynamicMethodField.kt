package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor

class DynamicMethodField(
  type: JavaType,
  name: String,
  owner: JavaType,
  _getterMethod: MarcelMethod?,
  _setterMethod: MarcelMethod?
) : MethodField(type, name, owner, _getterMethod, _setterMethod, false) {
  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

}
