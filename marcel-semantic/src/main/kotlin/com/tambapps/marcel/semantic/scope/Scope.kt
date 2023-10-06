package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable
import java.util.concurrent.ThreadLocalRandom

interface Scope {

  fun addLocalVariable(type: JavaType, isFinal: Boolean = false, token: LexToken = LexToken.dummy()): LocalVariable {
    val name = generateLocalVarName()
    return addLocalVariable(type, name, isFinal, token)
  }

  fun addLocalVariable(type: JavaType, name: String, isFinal: Boolean = false, token: LexToken): LocalVariable
  private fun generateLocalVarName(): String {
    return  "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
  }

  fun freeVariable(name: String)

  fun findMethodOrThrow(name: String, argumentTypes: List<JavaTyped>, node: Ast2Node): JavaMethod {
    // find first on class, then on imports, then on extensions
    return findMethod(name, argumentTypes) ?: throw MarcelSemanticException(node.token, "Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(name: String, argumentTypes: List<JavaTyped>): JavaMethod?

  fun findVariableOrThrow(name: String, node: Ast2Node): Variable {
    return findVariable(name) ?: throw MarcelSemanticException(node.token, "Variable $name is not defined")
  }

  fun findLocalVariable(name: String): LocalVariable?

  fun hasVariable(name: String): Boolean {
    return findVariable(name) != null
  }

  fun findVariable(name: String): Variable?
}