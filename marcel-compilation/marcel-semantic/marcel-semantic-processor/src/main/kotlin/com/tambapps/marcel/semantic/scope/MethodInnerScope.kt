package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.variable.LocalVariable

/**
 * An inner scope inside a method. E.g. in a if/else, in a switch, ...
 */
open class MethodInnerScope(
  parentScope: MethodScope,
  isInLoop: Boolean = false,
  isAsync: Boolean = false,
  imports: List<ImportNode>,
) : MethodScope(
  parentScope, parentScope.method, parentScope.symbolResolver, parentScope.classType,
  imports, parentScope.staticContext, parentScope.localVariablePool, parentScope.isAsync || isAsync
) {

  constructor(
    parentScope: MethodScope,
    isInLoop: Boolean = false,
    isAsync: Boolean = false,
  ) : this(parentScope, isInLoop, isAsync, parentScope.imports)

  override val localVariablesSnapshot: List<LocalVariable>
    get() = (parentScope as MethodScope).localVariablesSnapshot + super.localVariablesSnapshot

  private val _isInLoop = isInLoop
  val isInLoop: Boolean get() = _isInLoop || (parentScope as? MethodInnerScope)?.isInLoop ?: false

}