package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import java.util.concurrent.ThreadLocalRandom

/**
 * The scope inside a method
 */
open class MethodScope internal constructor(
  private val parentScope: Scope?,
  val method: JavaMethod,
  typeResolver: JavaTypeResolver,
  classType: JavaType,
  imports: List<ImportNode>,
  internal val staticContext: Boolean,
  internal val localVariablePool: LocalVariablePool,
): AbstractScope(typeResolver, classType, imports) {

  constructor(classScope: ClassScope, method: JavaMethod)
      : this(classScope, method, classScope.typeResolver, classScope.classType, classScope.imports, method.isStatic, LocalVariablePool(method.isStatic))

  private val localVariables = mutableListOf<LocalVariable>()

  fun addLocalVariable(type: JavaType, isFinal: Boolean = false, token: LexToken = LexToken.dummy()): LocalVariable {
    val name = generateLocalVarName()
    return addLocalVariable(type, name, isFinal, token)
  }

  private fun generateLocalVarName(): String {
    return  "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
  }

  fun addLocalVariable(type: JavaType, name: String, isFinal: Boolean = false, token: LexToken = LexToken.dummy()): LocalVariable {
    if (findLocalVariable(name) != null) {
      throw MarcelSemanticException(token, "A variable with name $name is already defined")
    }
    val v = localVariablePool.obtain(type, name, isFinal)
    localVariables.add(v)
    return v
  }

  fun freeLocalVariable(name: String) {
    val v = localVariables.find { it.name == name }
    if (v != null) {
      localVariables.remove(v)
      localVariablePool.free(v)
    }
  }

  fun findLocalVariable(name: String): LocalVariable? {
    return localVariables.find { it.name == name }
      // for MethodInnerScope
      ?: (parentScope as? MethodScope)?.findLocalVariable(name)
  }

  override fun findVariable(name: String) = findLocalVariable(name) ?: parentScope?.findVariable(name)
}