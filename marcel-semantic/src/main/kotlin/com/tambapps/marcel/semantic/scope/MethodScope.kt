package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeNode
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
  protected val parentScope: Scope,
  val method: JavaMethod,
  typeResolver: JavaTypeResolver,
  override val classType: JavaType,
  imports: List<ImportNode>,
  internal val staticContext: Boolean,
  internal val localVariablePool: LocalVariablePool,
): AbstractScope(typeResolver, classType.packageName, imports) {

  override val forExtensionType = parentScope.forExtensionType

  constructor(classScope: ClassScope, method: JavaMethod)
      : this(classScope, method, classScope.typeResolver, classScope.classType, classScope.imports, method.isStatic, LocalVariablePool(method.isStatic)) {
    // method parameters are stored in local variables
    for (param in method.parameters) {
      addLocalVariable(param.type, param.name, param.isFinal)
    }
  }

  val isStatic get() = method.isStatic
  private val localVariables = mutableListOf<LocalVariable>()

  // returns all the local variables at a particular moment
  open val localVariablesSnapshot get() = localVariables.toList()

  inline fun <T> useTempLocalVariable(type: JavaType, isFinal: Boolean = false, runnable: (LocalVariable) -> T): T {
    val v = addLocalVariable(type, isFinal)
    try {
      return runnable.invoke(v)
    } finally {
      freeLocalVariable(v.name)
    }
  }

  fun addLocalVariable(type: JavaType, isFinal: Boolean = false, token: LexToken = LexToken.DUMMY): LocalVariable {
    val name = generateLocalVarName()
    return addLocalVariable(type, name, isFinal, token)
  }

  private fun generateLocalVarName(): String {
    return  "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
  }

  fun addLocalVariable(type: JavaType, name: String, isFinal: Boolean = false, token: LexToken = LexToken.DUMMY): LocalVariable {
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
      freeLocalVariable(v)
    }
  }

  private fun freeLocalVariable(v: LocalVariable) {
    localVariables.remove(v)
    localVariablePool.free(v)
  }

  override fun findLocalVariable(name: String): LocalVariable? {
    return localVariables.find { it.name == name }
      // for MethodInnerScope
      ?: (parentScope as? MethodScope)?.findLocalVariable(name)
  }

  override fun findField(name: String) = parentScope.findField(name)

  override fun dispose() {
    val vars = localVariables.toList() // copy them to avoid modifying original list while iterating on it
    vars.forEach { freeLocalVariable(it) }
  }

  override fun resolveTypeOrThrow(node: TypeNode): JavaType {
    // we want the class scope to find the type as it can also resolve inner classes
    return parentScope.resolveTypeOrThrow(node)
  }
}