package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.type.JavaType

class LocalVariablePool {
  private val variablePool = mutableSetOf<LocalVariable>()
  private var maxSlot = 0

  fun obtain(type: JavaType, name: String): LocalVariable {
    val nbSlots = nbSlots(type)
    val v = variablePool.find { it.nbSlots == nbSlots }
    return if (v != null) {
      variablePool.remove(v)
      v.type = type
      v.name = name
      v
    } else newVariable(type, name)
  }

  fun free(localVariable: LocalVariable) {
    variablePool.add(localVariable)
  }

  private fun newVariable(type: JavaType, name: String): LocalVariable {
    val nbSlots = nbSlots(type)
    val index = maxSlot
    maxSlot+= nbSlots
    return LocalVariable(type, name, nbSlots, index)
  }

  private fun nbSlots(type: JavaType) = if (type == JavaType.long || type == JavaType.double) 2 else 1
}