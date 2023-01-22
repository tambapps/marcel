package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import org.objectweb.asm.Label

open class Scope constructor(val imports: MutableList<ImportNode>, val classType: JavaType, val superClassInternalName: String, val classMethods: List<JavaMethod>) {
  constructor(): this(mutableListOf(), JavaType("Test"), JavaType.Object.internalName, emptyList()) {
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

  fun getLocalVariable(name: String): LocalVariable {
    return getLocalVariableWithIndex(name).first
  }

  fun getMethodForType(type: JavaType, name: String, argumentTypes: List<TypedNode>): JavaMethod {
    if (type == classType) {
      return getMethod(name, argumentTypes)
    }
    val clazz = try {
      Class.forName(type.className)
    } catch (e: ClassNotFoundException) {
      throw SemanticException("Unknown class $type")
    }

    return try {
      return if (name == JavaMethod.CONSTRUCTOR_NAME) ReflectJavaConstructor(clazz.getDeclaredConstructor(*argumentTypes.map { it.type.realClassOrObject }.toTypedArray()))
      else ReflectJavaMethod(clazz.getDeclaredMethod(name, *argumentTypes.map { it.type.realClassOrObject }.toTypedArray()))
    } catch (e: NoSuchMethodException) {
      throw SemanticException("Unknown method " + e.message)
    }
  }

  fun getMethod(name: String, argumentTypes: List<TypedNode>): JavaMethod {
    return (classMethods.find { it.matches(name, argumentTypes) }
      // fallback on static imported method
      ?: imports.asSequence().mapNotNull { it.resolveMethod(name, argumentTypes) }.firstOrNull())
      ?: throw SemanticException("Method $name is not defined")
  }
  fun getLocalVariableWithIndex(name: String): Pair<LocalVariable, Int> {
    var index = 0
    for (variable in localVariables) {
      if (variable.name == name) return Pair(variable, index)
      index+= variable.nbSlots
    }
    throw SemanticException("Variable $name is not defined")
  }

  fun getLocalVariableIndex(name: String): Int {
    return getLocalVariableWithIndex(name).second
  }

  fun copy(): Scope {
    return Scope(imports, classType, superClassInternalName, classMethods)
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
      JavaType(Class.forName(resolveClassName(name)))
    } catch (e: ClassNotFoundException) {
      null
    }
  }

}

open class MethodScope(imports: MutableList<ImportNode>, classType: JavaType, superClassInternalName: String, classMethods: List<JavaMethod>, val methodName: String,
                       val parameters: List<MethodParameter>, val returnType: JavaType)
  : Scope(imports, classType, superClassInternalName, classMethods) {

  val localVariablesCount: Int
    get() = localVariables.size + innerScopeVariablesCount
  internal var innerScopeVariablesCount = 0

    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType):
        this(scope.imports, scope.classType, scope.superClassInternalName, scope.classMethods, methodName, parameters, returnType)
}

class InnerScope constructor(private val parentScope: MethodScope)
  : MethodScope(parentScope.imports, parentScope.classType, parentScope.superClassInternalName, parentScope.classMethods, parentScope.methodName, parentScope.parameters, parentScope.returnType) {

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
    parentScope.innerScopeVariablesCount++
    return variable
  }

  // to clean variables defined in inner scope, once we don't need the inner scope anymore
  fun clearInnerScopeLocalVariables() {
    innerScopeLocalVariables.forEach { localVariables.remove(it) }
  }
}
