package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType

class Scope(val classMethods: List<MethodNode>) {
  constructor(): this(emptyList())

  constructor(classNode: ClassNode): this(classNode.methods)

  private val localVariables: LinkedHashMap<String, LocalVariable> = LinkedHashMap()
  val localVariablesCount: Int
    get() = localVariables.size

  fun addLocalVariable(type: JavaType, name: String) {
    if (localVariables.containsKey(name)) {
      throw SemanticException("a variable with name $name is already defined")
    }
    localVariables[name] = LocalVariable(type, name)
  }

  fun getLocalVariable(name: String): LocalVariable {
    return localVariables[name] ?: throw SemanticException("Variable $name is not defined")
  }

  fun getMethod(name: String): MethodNode {
    return classMethods.find { it.name == name } ?: throw SemanticException("Method $name is not defined")
  }
  fun getLocalVariableWithIndex(name: String): Pair<LocalVariable, Int> {
    val variable = getLocalVariable(name)
    return Pair(variable, getLocalVariableIndex(name))
  }

  fun getLocalVariableIndex(name: String): Int {
    return localVariables.values.indexOfFirst { it.name == name }
  }
}