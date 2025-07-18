package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import java.util.concurrent.ThreadLocalRandom

/**
 * The scope inside a method
 */
open class MethodScope internal constructor(
  protected val parentScope: Scope,
  val method: MarcelMethod,
  symbolResolver: MarcelSymbolResolver,
  override val classType: JavaType,
  importResolver: ImportResolver,
  val staticContext: Boolean,
  internal val localVariablePool: LocalVariablePool,
  val isAsync: Boolean,
) : AbstractScope(symbolResolver, classType.packageName, importResolver) {

  override val forExtensionType = parentScope.forExtensionType
  open val isInLambda get() = false

  constructor(classScope: ClassScope, method: MarcelMethod)
      : this(
    classScope, method, classScope.symbolResolver, classScope.classType, classScope.importResolver,
    staticContext = method.isStatic, LocalVariablePool(method.isStatic), isAsync = method.isAsync
  ) {
    // method parameters are stored in local variables
    for (param in method.parameters) {
      addLocalVariable(param.type, param.name, param.nullness, param.isFinal)
    }
  }

  private val localVariables = mutableListOf<LocalVariable>()

  // returns all the local variables at a particular moment
  open val localVariablesSnapshot: List<LocalVariable>
    get() {
      val variables = mutableListOf<LocalVariable>()
      var scope: MethodScope? = this
      while (scope != null) {
        variables.addAll(scope.localVariables)
        scope = scope.parentScope as? MethodScope
      }
      return variables
    }

  inline fun <T> useTempLocalVariable(type: JavaType, nullness: Nullness, isFinal: Boolean = false, runnable: (LocalVariable) -> T): T {
    val v = addLocalVariable(type, nullness, isFinal)
    try {
      return runnable.invoke(v)
    } finally {
      freeLocalVariable(v.name)
    }
  }

  fun addLocalVariable(type: JavaType, nullness: Nullness, isFinal: Boolean = false, token: LexToken = LexToken.DUMMY): LocalVariable {
    val name = generateLocalVarName()
    return addLocalVariable(type, name, nullness, isFinal, token)
  }

  private fun generateLocalVarName(): String {
    return "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
  }

  fun addLocalVariable(
    type: JavaType,
    name: String,
    nullness: Nullness,
    isFinal: Boolean = false,
    token: LexToken = LexToken.DUMMY
  ): LocalVariable {
    if (findLocalVariable(name) != null) {
      throw MarcelSemanticException(token, "A variable with name $name is already defined")
    }
    val v = localVariablePool.obtain(type, name, nullness, isFinal)
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

  fun getMethodParameterVariable(i: Int) = findLocalVariable(method.parameters[i].name)!!

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