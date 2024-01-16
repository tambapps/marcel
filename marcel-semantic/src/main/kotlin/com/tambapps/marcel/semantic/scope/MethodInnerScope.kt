package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.variable.LocalVariable

/**
 * An inner scope inside a method. E.g. in a if/else, in a switch, ...
 */
open class MethodInnerScope(
  parentScope: MethodScope,
  isInLoop: Boolean = false,
  isAsync: Boolean = false,
) : MethodScope(parentScope, parentScope.method, parentScope.symbolResolver, parentScope.classType,
  parentScope.imports, parentScope.staticContext, parentScope.localVariablePool, parentScope.isAsync || isAsync) {

  override val localVariablesSnapshot: List<LocalVariable>
    get() = (parentScope as MethodScope).localVariablesSnapshot + super.localVariablesSnapshot

  private val _isInLoop = isInLoop
  val isInLoop: Boolean get() = _isInLoop || (parentScope as? MethodInnerScope)?.isInLoop ?: false

}