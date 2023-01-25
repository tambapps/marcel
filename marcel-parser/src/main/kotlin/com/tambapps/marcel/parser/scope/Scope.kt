package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Label

// note that extensionMethods may not be needed. It could be added directly on the Java Type
open class Scope constructor(val imports: MutableList<ImportNode>, val classType: JavaType, val superClassInternalName: String, val extensionMethods: MutableList<JavaMethod>) {
  constructor(javaType: JavaType): this(mutableListOf(), javaType, JavaType.Object.internalName, mutableListOf()) {
    imports.addAll(DEFAULT_IMPORTS)
  }

  companion object {
    val DEFAULT_IMPORTS = listOf(
      WildcardImportNode("java.lang"),
      WildcardImportNode("java.util"),
      WildcardImportNode("marcel.lang"),
      WildcardImportNode("marcel.lang"),
      WildcardImportNode("it.unimi.dsi.fastutil.ints"),
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
    return v
  }

  fun removeVariable(name: String) {
    localVariables.removeIf { it.name == name }
  }

  fun getMethod(name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    // find first on class, then on imports, then on extensions
    return (classType.findMethod(name, argumentTypes, true)
      // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(name, argumentTypes) }.firstOrNull())
      ?: extensionMethods.find { it.matches(name, argumentTypes) }
      ?: throw SemanticException("Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }
  fun findVariable(name: String): Variable {
    var index = 0
    for (variable in localVariables) {
      if (variable.name == name) {
        variable.index = index
        return variable
      }
      index+= variable.nbSlots
    }
    // TODO for now only searching on local variables but will need to search on fields
    throw SemanticException("Variable $name is not defined")
  }

  fun copy(): Scope {
    return Scope(imports, classType, superClassInternalName, extensionMethods)
  }

  fun resolveClassName(classSimpleName: String): String {
    val matchedClasses = imports.mapNotNull { it.resolveClassName(classSimpleName) }.toSet()
    if (matchedClasses.isEmpty()) {
      return classSimpleName
    } else if (matchedClasses.size == 1) {
      return matchedClasses.first()
    } else {
      throw SemanticException("Ambiguous import for class $classSimpleName")
    }
  }

  fun getTypeOrNull(name: String): JavaType? {
    return try {
      JavaType.of(Class.forName(resolveClassName(name)))
    } catch (e: ClassNotFoundException) {
      null
    }
  }

}

open class MethodScope(imports: MutableList<ImportNode>, classType: JavaType, superClassInternalName: String, classMethods: MutableList<JavaMethod>, val methodName: String,
                       val parameters: List<MethodParameter>, val returnType: JavaType)
  : Scope(imports, classType, superClassInternalName, classMethods) {

    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType):
        this(scope.imports, scope.classType, scope.superClassInternalName, scope.extensionMethods, methodName, parameters, returnType)
}

class InnerScope constructor(private val parentScope: MethodScope)
  : MethodScope(parentScope.imports, parentScope.classType, parentScope.superClassInternalName, parentScope.extensionMethods, parentScope.methodName, parentScope.parameters, parentScope.returnType) {

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

  // to clean variables defined in inner scope, once we don't need the inner scope anymore
  fun clearInnerScopeLocalVariables() {
    innerScopeLocalVariables.forEach { localVariables.remove(it) }
  }
}
