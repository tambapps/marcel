package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType

open class Scope constructor(val imports: List<ImportNode>, val className: String, val superClassInternalName: String, val classMethods: List<MethodNode>) {
  constructor(): this(emptyList(), "Test", JavaType.OBJECT.internalName, emptyList())

  constructor( className: String, imports: List<ImportNode>, classNode: ClassNode): this(imports, className, classNode.parentType.internalName, classNode.methods)

  private val localVariables: LinkedHashMap<String, LocalVariable> = LinkedHashMap()
  val localVariablesCount: Int
    get() = localVariables.size

  fun addLocalVariable(type: JavaType, name: String): LocalVariable {
    if (localVariables.containsKey(name)) {
      throw SemanticException("a variable with name $name is already defined")
    }
    val v = LocalVariable(type, name)
    localVariables[name] = v
    return v
  }

  fun getLocalVariable(name: String): LocalVariable {
    return localVariables[name] ?: throw SemanticException("Variable $name is not defined")
  }

  fun getMethodForType(type: JavaType, name: String, argumentTypes: List<TypedNode>): MethodNode {
    // TODO for now only searching on outside classes
    val clazz = try {
      Class.forName(type.className)
    } catch (e: ClassNotFoundException) {
      throw SemanticException("Unkown class $type")
    }
    val method = clazz.getDeclaredMethod(name, *argumentTypes.map { it.type.realClassOrObject }.toTypedArray())
    TODO("I was here")
  }

  fun getMethod(name: String, argumentTypes: List<TypedNode>): MethodNode {
    return classMethods.find { it.matches(name, argumentTypes) } ?: throw SemanticException("Method $name is not defined")
  }
  fun getLocalVariableWithIndex(name: String): Pair<LocalVariable, Int> {
    val variable = getLocalVariable(name)
    return Pair(variable, getLocalVariableIndex(name))
  }

  fun getLocalVariableIndex(name: String): Int {
    return localVariables.values.indexOfFirst { it.name == name }
  }

  fun copy(): Scope {
    return Scope(imports, className, superClassInternalName, classMethods)
  }

  fun resolveClassName(classSimpleName: String): String {
    val matchedClasses = imports.mapNotNull { it.resolve(classSimpleName) }.toSet()
    if (matchedClasses.isEmpty()) {
      return classSimpleName
    } else if (matchedClasses.size == 1) {
      return matchedClasses.first()
    } else {
      throw SemanticException("Ambiguous import for class $classSimpleName")
    }
  }

}

class MethodScope(imports: List<ImportNode>, className: String, superClassInternalName: String, classMethods: List<MethodNode>, val methodName: String,
  val parameters: List<MethodParameter>, val returnType: JavaType)
  : Scope(imports, className, superClassInternalName, classMethods) {
    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType):
        this(scope.imports, scope.className, scope.superClassInternalName, scope.classMethods, methodName, parameters, returnType)
}
