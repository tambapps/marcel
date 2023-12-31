package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import java.util.concurrent.ThreadLocalRandom

/**
 * The scope inside a method
 */
open class MethodScope internal constructor(
  protected val parentScope: Scope,
  val method: JavaMethod,
  symbolResolver: MarcelSymbolResolver,
  override val classType: JavaType,
  imports: List<ImportNode>,
  internal val staticContext: Boolean,
  internal val localVariablePool: LocalVariablePool,
): AbstractScope(symbolResolver, classType.packageName, imports) {

  override val forExtensionType = parentScope.forExtensionType

  constructor(classScope: ClassScope, method: JavaMethod)
      : this(classScope, method, classScope.symbolResolver, classScope.classType, classScope.imports, method.isStatic, LocalVariablePool(method.isStatic)) {
    // method parameters are stored in local variables
    for (param in method.parameters) {
      addLocalVariable(param.type, param.name, param.isFinal)
    }
  }

  val isStatic get() = method.isStatic
  private val localVariables = mutableListOf<LocalVariable>()

  // returns all the local variables at a particular moment
  open val localVariablesSnapshot: List<LocalVariable> get() {
    val variables = mutableListOf<LocalVariable>()
    var scope: MethodScope? = this
    while (scope != null) {
      variables.addAll(scope.localVariables)
      scope = scope.parentScope as? MethodScope
    }
    return variables
  }

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

  override fun resolveTypeOrThrow(node: TypeCstNode): JavaType {
    // we want the class scope to find the type as it can also resolve inner classes
    return parentScope.resolveTypeOrThrow(node)
  }
}