package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.StaticImportNode
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.methods.DefaultMarcelStaticMethods
import org.objectweb.asm.Label
import java.util.concurrent.ThreadLocalRandom

open class Scope constructor(val typeResolver: AstNodeTypeResolver, val imports: MutableList<ImportNode>, open val classType: JavaType, val superClass: JavaType) {
  constructor(typeResolver: AstNodeTypeResolver, javaType: JavaType): this(typeResolver, mutableListOf(), javaType, JavaType.Object) {
    imports.addAll(DEFAULT_IMPORTS)
  }
  constructor(typeResolver: AstNodeTypeResolver, javaType: JavaType, imports: List<ImportNode>): this(typeResolver, javaType) {
    this.imports.addAll(imports)
  }
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
    val name = "__marcel_unnamed_" + this.hashCode().toString().replace('-', '_') + '_' +
        ThreadLocalRandom.current().nextInt().toString().replace('-', '_')
    return addLocalVariable(type, name, isFinal)
  }
  open fun addLocalVariable(type: JavaType, name: String, isFinal: Boolean = false): LocalVariable {
    if (localVariables.any { it.name == name }) {
      throw SemanticException("A variable with name $name is already defined")
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

  fun getMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    // find first on class, then on imports, then on extensions
    return (typeResolver.findMethod(classType, name, argumentTypes)
      // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, argumentTypes) }.firstOrNull())
      ?: throw SemanticException("Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }
  open fun findLocalVariable(name: String): LocalVariable? {
    return localVariables.find { it.name == name }
  }

  open fun findVariable(name: String): Variable {
    val localVariable = findLocalVariable(name)
    if (localVariable != null) return localVariable
    // now searching on fields
    return typeResolver.findField(classType, name, true) ?: throw SemanticException("Variable $name is not defined")
  }

  fun copy(t: JavaType? = null): Scope {
    val classType = t ?: this.classType

    return Scope(typeResolver, imports, classType, classType.superType ?: JavaType.Object)
  }

  fun resolveType(classSimpleName: String, genericTypes: List<JavaType>): JavaType {
    // try to find inner class with this name
    val innerClassName = classType.className + '$' + classSimpleName
    if (typeResolver.isDefined(innerClassName)) return typeResolver.of(innerClassName, genericTypes)
    val className = resolveClassName(classSimpleName)
    return typeResolver.of(className, genericTypes)
  }

  private fun resolveClassName(classSimpleName: String): String {
    val matchedClasses = imports.mapNotNull { it.resolveClassName(classSimpleName) }.toSet()
    return if (matchedClasses.isEmpty()) classSimpleName
    else if (matchedClasses.size == 1) matchedClasses.first()
    else throw SemanticException("Ambiguous import for class $classSimpleName")
  }

  fun getTypeOrNull(name: String): JavaType? {
    return try {
      resolveType(name, emptyList())
    } catch (e: SemanticException) {
      null
    }
  }

  fun <T> simulateVariable(type: JavaType, name: String, function: () -> T): T {
    val fakeVariable = LocalVariable(type, name, 0, 0, false)
    val optVar = localVariables.find { it.name == name }
    if (optVar != null) localVariables[localVariables.indexOf(optVar)] = fakeVariable
    else localVariables.add(fakeVariable)
    val result = function.invoke()
    if (optVar != null) localVariables[localVariables.indexOf(fakeVariable)] = optVar
    else localVariables.remove(fakeVariable)
    return result
  }
}

open class MethodScope constructor(typeResolver: AstNodeTypeResolver, imports: MutableList<ImportNode>, classType: JavaType, superClass: JavaType, val methodName: String,
                       val parameters: List<MethodParameter>, var returnType: JavaType)
  : Scope(typeResolver, imports, classType, superClass) {

    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType):
        this(scope.typeResolver, scope.imports, scope.classType, scope.superClass, methodName, parameters, returnType)
}

class LambdaScope constructor(val parentScope: Scope):
    Scope(parentScope.typeResolver, parentScope.classType, parentScope.imports)

class InnerScope constructor(private val parentScope: MethodScope)
  : MethodScope(parentScope.typeResolver, parentScope.imports, parentScope.classType, parentScope.superClass, parentScope.methodName, parentScope.parameters, parentScope.returnType) {

  override val localVariablePool = parentScope.localVariablePool

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
