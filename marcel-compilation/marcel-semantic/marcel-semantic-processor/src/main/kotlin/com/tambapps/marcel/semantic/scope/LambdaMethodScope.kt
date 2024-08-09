package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.variable.field.MarcelField

/**
 * Lambda method scope using local variables of where the lambda is called as immutable fields
 */
class LambdaMethodScope(
  classScope: ClassScope,
  method: MarcelMethod,
  // local variables from the method in which the lambda is called
  private val outerScopeLocalVariable: List<LocalVariable>
) : MethodScope(classScope, method) {
  private val usedOuterScopeLocalVariableMap = mutableMapOf<LocalVariable, MarcelField>()
  val usedOuterScopeLocalVariable get() = usedOuterScopeLocalVariableMap.keys

  override fun findField(name: String): MarcelField? {
    var f = super.findField(name)
    if (f == null) {
      f = outerScopeLocalVariable.find { it.name == name }?.let { lv ->
        usedOuterScopeLocalVariableMap.computeIfAbsent(lv) {
          JavaClassFieldImpl(
            it.type, it.name, isFinal = true, visibility = Visibility.PUBLIC,
            isStatic = false, owner = classType, isSettable = false
          )
        }
      }
    }
    return f
  }
}