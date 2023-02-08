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
      WildcardImportNode("marcel.lang"),
      WildcardImportNode("marcel.lang"),
      WildcardImportNode("it.unimi.dsi.fastutil.ints"),
      StaticImportNode(DefaultMarcelStaticMethods::class.java.name, "println")
    )
  }

  // Linked because we need it to be sorted by insertion order
  internal open val localVariables: MutableList<LocalVariable> = mutableListOf()

  open fun addLocalVariable(type: JavaType, name: String): LocalVariable {
    if (localVariables.any { it.name == name }) {
      throw SemanticException("A variable with name $name is already defined")
    }
    val v = LocalVariable(type, name)
    if (name == "this") {
      // this should always be the first element in the map
      localVariables.add(0, v)
    } else {
      localVariables.add(v)
    }
    //  calling findLocalVariable() because we need to compute variable index
    return findLocalVariable(name)!!
  }

  fun removeVariable(name: String) {
    localVariables.removeIf { it.name == name }
  }

  fun getMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    // find first on class, then on imports, then on extensions
    return (typeResolver.findMethod(classType, name, argumentTypes)
      // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(typeResolver, name, argumentTypes) }.firstOrNull())
      ?: throw SemanticException("Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }
  open fun findLocalVariable(name: String): LocalVariable? {
    var index = 0
    for (variable in localVariables) {
      if (variable.name == name) {
        variable.index = index
        return variable
      }
      index+= variable.nbSlots
    }
    return null
  }

  open fun findVariable(name: String): Variable {
    val localVariable = findLocalVariable(name)
    if (localVariable != null) return localVariable
    // now searching on fields
    return typeResolver.findField(classType, name, true) ?: throw SemanticException("Variable $name is not defined")
  }

  fun copy(): Scope {
    return Scope(typeResolver, imports, classType, superClass)
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

  fun tempVariable(type: JavaType, function: (LocalVariable) -> Unit) {
    val variable = LocalVariable(type, "__temp")
    localVariables.add(variable)
    function.invoke(variable)
    localVariables.remove(variable)
  }

}

open class MethodScope constructor(typeResolver: AstNodeTypeResolver, imports: MutableList<ImportNode>, classType: JavaType, superClass: JavaType, val methodName: String,
                       val parameters: List<MethodParameter>, val returnType: JavaType)
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

  // we want to access local variable defined in parent scope
  override val localVariables: MutableList<LocalVariable>
    get() = parentScope.localVariables

  private val innerScopeLocalVariables = mutableListOf<LocalVariable>()

  var continueLabel: Label? = null
    get() = if (field != null) field
      else if (parentScope is InnerScope) parentScope.continueLabel
      else null

  var breakLabel: Label? = null
    get() = if (field != null) field
    else if (parentScope is InnerScope) parentScope.breakLabel
    else null

  override fun addLocalVariable(type: JavaType, name: String): LocalVariable {
    val variable = super.addLocalVariable(type, name)
    innerScopeLocalVariables.add(variable)
    return variable
  }

  override fun findVariable(name: String): Variable {
    return super.findVariable(name)
  }
  // to clean variables defined in inner scope, once we don't need the inner scope anymore
  fun clearInnerScopeLocalVariables() {
    innerScopeLocalVariables.forEach { localVariables.remove(it) }
  }
}
