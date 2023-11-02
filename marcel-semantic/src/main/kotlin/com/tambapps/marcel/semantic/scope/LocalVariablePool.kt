package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable

/**
 * A pool allowing to keep record of local variable slots
 */
class LocalVariablePool(staticContext: Boolean) {
  private val variablePool = mutableSetOf<LocalVariable>()
  private var maxSlot = 0
  // yep for non-static slot starts at 1 because 0 is this
  private val slotStart = if (staticContext) 0 else 1

  fun obtain(type: JavaType, name: String, isFinal: Boolean): LocalVariable {
    val nbSlots = type.nbSlots
    val v = variablePool.find { it.nbSlots == nbSlots }
    return if (v != null) {
      variablePool.remove(v)
      LocalVariable(type, name, nbSlots, v.index, isFinal)
    } else newVariable(type, name, isFinal)
  }

  fun free(localVariable: LocalVariable) {
    variablePool.add(localVariable)
  }

  private fun newVariable(type: JavaType, name: String, isFinal: Boolean): LocalVariable {
    val nbSlots = type.nbSlots
    val index = slotStart + maxSlot // "1 +" because the slot 0 is reserved for this
    maxSlot+= nbSlots
    return LocalVariable(type, name, nbSlots, index, isFinal)
  }
}