package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod

open class Scope constructor(val imports: List<ImportNode>, val className: String, val superClassInternalName: String, val classMethods: List<JavaMethod>) {
  constructor(): this(emptyList(), "Test", JavaType.OBJECT.internalName, emptyList())

  constructor(className: String, imports: List<ImportNode>, classNode: ClassNode): this(imports, className, classNode.parentType.internalName, classNode.methods)

  protected val localVariables: LinkedHashMap<String, LocalVariable> = LinkedHashMap()
  val localVariablesCount: Int
    get() = localVariables.size

  fun addLocalVariable(type: JavaType, name: String): LocalVariable {
    if (localVariables.containsKey(name)) {
      throw SemanticException("A variable with name $name is already defined")
    }
    val v = LocalVariable(type, name)
    localVariables[name] = v
    return v
  }

  fun getLocalVariable(name: String): LocalVariable {
    return localVariables[name] ?: throw SemanticException("Variable $name is not defined")
  }

  fun getMethodForType(type: JavaType, name: String, argumentTypes: List<TypedNode>): JavaMethod {
    if (type.className == className) {
      return getMethod(name, argumentTypes)
    }
    val clazz = try {
      Class.forName(type.className)
    } catch (e: ClassNotFoundException) {
      throw SemanticException("Unknown class $type")
    }

    return if (name == JavaMethod.CONSTRUCTOR_NAME) ReflectJavaConstructor(clazz.getDeclaredConstructor(*argumentTypes.map { it.type.realClassOrObject }.toTypedArray()))
    else ReflectJavaMethod(clazz.getDeclaredMethod(name, *argumentTypes.map { it.type.realClassOrObject }.toTypedArray()))
  }

  fun getMethod(name: String, argumentTypes: List<TypedNode>): JavaMethod {
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

class MethodScope(imports: List<ImportNode>, className: String, superClassInternalName: String, classMethods: List<JavaMethod>, val methodName: String,
  val parameters: List<MethodParameter>, val returnType: JavaType)
  : Scope(imports, className, superClassInternalName, classMethods) {

  constructor(methodScope: MethodScope) :
    this(methodScope, methodScope.methodName, methodScope.parameters, methodScope.returnType) {
    localVariables.putAll(methodScope.localVariables)
    }

    constructor(scope: Scope,
                methodName: String,
                parameters: List<MethodParameter>, returnType: JavaType):
        this(scope.imports, scope.className, scope.superClassInternalName, scope.classMethods, methodName, parameters, returnType)
}
