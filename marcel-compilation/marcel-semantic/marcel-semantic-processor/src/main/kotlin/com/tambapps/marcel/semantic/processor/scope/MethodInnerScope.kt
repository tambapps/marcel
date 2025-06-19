package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.variable.LocalVariable

/**
 * An inner scope inside a method. E.g. in a if/else, in a switch, ...
 */
open class MethodInnerScope(
  parentScope: MethodScope,
  isInLoop: Boolean = false,
  isAsync: Boolean = false,
  importResolver: ImportResolver,
) : MethodScope(
  parentScope, parentScope.method, parentScope.symbolResolver, parentScope.classType,
  importResolver, parentScope.staticContext, parentScope.localVariablePool,
  parentScope.isAsync || isAsync
) {

  constructor(
    parentScope: MethodScope,
    isInLoop: Boolean = false,
    isAsync: Boolean = false,
  ) : this(parentScope, isInLoop, isAsync, parentScope.importResolver)

  override val localVariablesSnapshot: List<LocalVariable>
    get() = (parentScope as MethodScope).localVariablesSnapshot + super.localVariablesSnapshot

  private val _isInLoop = isInLoop
  val isInLoop: Boolean get() = _isInLoop || (parentScope as? MethodInnerScope)?.isInLoop ?: false
  override val isInLambda = parentScope.isInLambda

}