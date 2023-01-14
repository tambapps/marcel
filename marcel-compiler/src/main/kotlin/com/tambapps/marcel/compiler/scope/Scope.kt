package com.tambapps.marcel.compiler.scope

import com.tambapps.marcel.compiler.exception.SemanticException
import com.tambapps.marcel.compiler.variable.LocalVariable
import com.tambapps.marcel.parser.type.JavaType

class Scope {
  private val localVariables: LinkedHashMap<String, LocalVariable> = LinkedHashMap()
  val localVariablesCount: Int
    get() = localVariables.size

  fun addLocalVariable(type: JavaType, name: String) {
    if (localVariables.containsKey(name)) {
      throw SemanticException("a variable with name $name is already defined")
    }
    localVariables[name] = LocalVariable(type, name)
  }

  fun getLocalVariableWithIndex(name: String): Pair<LocalVariable, Int> {
    val variable = localVariables[name] ?: throw SemanticException("Variable $name is not defined")
    return Pair(variable, getLocalVariableIndex(name))
  }

  fun getLocalVariableIndex(name: String): Int {
    return localVariables.values.indexOfFirst { it.name == name }
  }
}