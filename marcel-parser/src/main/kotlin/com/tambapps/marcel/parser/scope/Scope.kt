package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.ast.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.StaticImportNode
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.methods.DefaultMarcelStaticMethods
import org.objectweb.asm.Label
import java.util.concurrent.ThreadLocalRandom

open class Scope constructor(val typeResolver: AstNodeTypeResolver, val imports: MutableList<ImportNode>, open var classType: JavaType) {
  constructor(typeResolver: AstNodeTypeResolver, javaType: JavaType): this(typeResolver, mutableListOf(), javaType) {
    imports.addAll(DEFAULT_IMPORTS)
  }
  constructor(typeResolver: AstNodeTypeResolver, javaType: JavaType, imports: List<ImportNode>): this(typeResolver, javaType) {
    this.imports.addAll(imports)
  }

  val superClass get() = classType.superType!!

  companion object {
    val DEFAULT_IMPORTS = listOf(
      WildcardImportNode("java.lang"),
      WildcardImportNode("java.util"),
      WildcardImportNode("java.io"),
      WildcardImportNode("marcel.lang"),
      StaticImportNode(DefaultMarcelStaticMethods::class.java.name, "println")
    )
  }

  internal open val localVariablePool = LocalVariablePool()
  private val localVariables: MutableList<LocalVariable> = mutableListOf()

  open fun addLocalVariable(type: JavaType, isFinal: Boolean = false): LocalVariable {
    val name = generateLocalVarName()
    return addLocalVariable(type, name, isFinal)
  }
  private fun generateLocalVarName(): String {
    return  "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
  }

  open fun addLocalVariable(type: JavaType, name: String, isFinal: Boolean = false): LocalVariable {
    if (localVariables.any { it.name == name }) {
      throw MarcelSemanticException("A variable with name $name is already defined")
    }
    val v = localVariablePool.obtain(type, name, isFinal)
    localVariables.add(v)
    return v
  }

  fun freeVariable(name: String) {
    val v = localVariables.find { it.name == name }
    if (v != null) {
      localVariables.remove(v)
      localVariablePool.free(v)
    }
  }

  fun getMethodWithParameters(name: String, positionalArgumentTypes: List<AstTypedObject>, namedParameters: List<MethodParameter>): JavaMethod {
    return typeResolver.findMethodByParameters(classType, name, positionalArgumentTypes, namedParameters)
      ?: throw MarcelSemanticException("Method $name with parameters $namedParameters is not defined")
  }

  fun findMethodOrThrow(name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    // find first on class, then on imports, then on extensions
    return findMethod(name, argumentTypes) ?: throw MarcelSemanticException("Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
   return typeResolver.findMethod(classType, name, argumentTypes)
    // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, argumentTypes) }.firstOrNull()
  }


  open fun findLocalVariable(name: String): LocalVariable? {
    return localVariables.find { it.name == name }
  }

  open fun findVariableOrThrow(name: String): Variable {
    return findVariable(name) ?: throw MarcelSemanticException("Variable $name is not defined")
  }

  fun hasVariable(name: String): Boolean {
    return findVariable(name) != null
  }

  open fun findVariable(name: String): Variable? {
    val localVariable = findLocalVariable(name)
    if (localVariable != null) return localVariable
    // now searching on fields
    return typeResolver.findField(classType, name, true)
  }

  fun copy(t: JavaType? = null): Scope {
    val classType = t ?: this.classType

    return Scope(typeResolver, imports, classType)
  }

  fun resolveType(classSimpleName: String, genericTypes: List<JavaType>): JavaType {
    // try to find inner class with this name
    val innerClassName = if (classType.innerName == classSimpleName) classType.className
    else classType.className + '$' + classSimpleName
    if (typeResolver.isDefined(innerClassName)) return typeResolver.of(innerClassName, genericTypes)
    if (typeResolver.isDefined("${classType.packageName}.$classSimpleName")) return typeResolver.of("${classType.packageName}.$classSimpleName", genericTypes)
    val className = resolveClassName(classSimpleName)
    return typeResolver.of(className, genericTypes)
  }

  private fun resolveClassName(classSimpleName: String): String {
    val matchedClasses = imports.mapNotNull { it.resolveClassName(typeResolver, classSimpleName) }.toSet()
    return if (matchedClasses.isEmpty()) classSimpleName
    else if (matchedClasses.size == 1) matchedClasses.first()
    else throw MarcelSemanticException("Ambiguous import for class $classSimpleName")
  }

  fun getTypeOrNull(name: String): JavaType? {
    return try {
      resolveType(name, emptyList())
    } catch (e: MarcelSemanticException) {
      null
    }
  }

  fun <T> simulateVariable(type: JavaType, function: (LocalVariable) -> T): T {
    return simulateVariable(type, generateLocalVarName(), function)
  }
  fun <T> simulateVariable(type: JavaType, name: String, function: (LocalVariable) -> T): T {
    val fakeVariable = LocalVariable(type, name, 0, 0, false)
    val optVar = localVariables.find { it.name == name }
    if (optVar != null) localVariables[localVariables.indexOf(optVar)] = fakeVariable
    else localVariables.add(fakeVariable)
    val result = function.invoke(fakeVariable)
    if (optVar != null) localVariables[localVariables.indexOf(fakeVariable)] = optVar
    else localVariables.remove(fakeVariable)
    return result
  }
}

open class MethodScope constructor(typeResolver: AstNodeTypeResolver, imports: MutableList<ImportNode>, classType: JavaType, val methodName: String,
                                   val parameters: List<MethodParameter>, var returnType: JavaType,
                                   // because we don't want to define parameters in the Parsing phase, as we might need to resolve types for that
                                   defineParametersAutomatically: Boolean = true)
  : Scope(typeResolver, imports, classType) {

    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType, defineParametersAutomatically: Boolean = true):
        this(scope.typeResolver, scope.imports, scope.classType, methodName, parameters, returnType, defineParametersAutomatically)

  private var methodParametersDefined = false
  init {
      if (defineParametersAutomatically) {
        // method parameters are (at least they should be) automatically defined only for methods generated while compiling
        // for other methods (method that we dot from parsing) we define them manually when starting the compiling phase. This is to avoid resolving types
        // while parsing
        defineParametersInScope()
      }
  }
  fun defineParametersInScope() {
    if (methodParametersDefined) return
    for (param in parameters) {
      addLocalVariable(param.type, param.name, param.isFinal)
    }
    methodParametersDefined = true
  }
}

class LambdaScope constructor(val parentScope: Scope):
    Scope(parentScope.typeResolver, parentScope.classType, parentScope.imports) {
  override var classType: JavaType
    get() = parentScope.classType
    set(value) { parentScope.classType = value }

}

class InnerScope constructor(private val parentScope: MethodScope)
  : MethodScope(parentScope.typeResolver, parentScope.imports, parentScope.classType, parentScope.methodName, parentScope.parameters, parentScope.returnType, false) {

  override val localVariablePool = parentScope.localVariablePool
  override var classType: JavaType
    get() = parentScope.classType
    set(value) { parentScope.classType = value }

  var continueLabel: Label? = null
    get() = if (field != null) field
      else if (parentScope is InnerScope) parentScope.continueLabel
      else null

  var breakLabel: Label? = null
    get() = if (field != null) field
    else if (parentScope is InnerScope) parentScope.breakLabel
    else null

  override fun findLocalVariable(name: String): LocalVariable? {
    return parentScope.findLocalVariable(name) ?: super.findLocalVariable(name)
  }

}
