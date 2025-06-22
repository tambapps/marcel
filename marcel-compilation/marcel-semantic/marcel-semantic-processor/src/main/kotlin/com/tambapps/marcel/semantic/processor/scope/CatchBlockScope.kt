package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.semantic.symbol.variable.LocalVariable

/**
 * Scope for a catch/finally block
 */
class CatchBlockScope(
  parentScope: MethodScope,
  // variables to ignore, as they must not be referencable in a catch block
  private val resourceVariableNames: Set<String>
) : MethodInnerScope(parentScope, false) {

  override fun findLocalVariable(name: String): LocalVariable? {
    return if (resourceVariableNames.contains(name)) null else super.findLocalVariable(name)
  }
}