package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField

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
  override val isInLambda get() = true

  override fun findField(name: String): MarcelField? {
    // local variables of outer scope come first
    return outerScopeLocalVariable.find { it.name == name }?.let { lv ->
      usedOuterScopeLocalVariableMap.computeIfAbsent(lv) {
        JavaClassFieldImpl(
          it.type, it.name, nullness = lv.nullness, isFinal = true, visibility = Visibility.PUBLIC,
          isStatic = false, owner = classType, isSettable = false
        )
      }
      // then we search on the fields
    } ?: super.findField(name)
  }
}