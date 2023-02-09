package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.getElementsType
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getMethod
import com.tambapps.marcel.compiler.util.getValuesType
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod

class JavaTypeResolver: AstNodeTypeResolver() {

  private val classMethods = mutableMapOf<String, MutableList<JavaMethod>>()

  override fun defineMethod(javaType: JavaType, method: JavaMethod) {
    val methods = getMarcelMethods(javaType)
    if (methods.any { it.matches(this, method.name, method.parameters) }) {
      throw SemanticException("Method with $method is already defined")
    }
    methods.add(method)
  }

  override fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else classMethods[javaType.className] ?: emptyList()
  }

  override fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean): JavaMethod? {
    val methods = getMarcelMethods(javaType)
    var m = methods.find { it.matches(this, name, argumentTypes) }
    if (m != null) return m

    if (javaType.isLoaded) {
      val clazz = javaType.type.realClazz
      val candidates = if (name == JavaMethod.CONSTRUCTOR_NAME) {
        clazz.declaredConstructors
          .map { ReflectJavaConstructor(it) }
          .filter { it.matches(this, argumentTypes) }
      } else {
        clazz.declaredMethods
          .filter { it.name == name }
          .map { ReflectJavaMethod(it, javaType) }
          .filter { it.matches(this, argumentTypes) }
      }
      m = getMoreSpecificMethod(candidates)
      if (m != null) return m
    }

    // search in super types
    var type = javaType.superType
    while (type != null) {
      m = findMethod(type, name, argumentTypes, true)
      if (m != null) return m
      type = type.superType
    }

    if (excludeInterfaces) return null
    // now search on all implemented interfaces
    for (interfaze in javaType.allImplementedInterfaces) {
      m = findMethod(interfaze, name, argumentTypes)
      if (m != null) return m
    }
    return null
  }

  private fun getMoreSpecificMethod(candidates: List<JavaMethod>): JavaMethod? {
    // inspired from Class.searchMethods()
    var m: JavaMethod? = null
    for (candidate in candidates) {
      if (m == null
        || (m.returnType != candidate.returnType
            && m.returnType.isAssignableFrom(candidate.returnType))) m = candidate
    }
    return m
  }

  private fun getMarcelMethods(javaType: JavaType): MutableList<JavaMethod> {
    // return methods defined from MDK or from marcel source we're currently compiling
    return classMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  override fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
    if (javaType.isLoaded) {
      val clazz = javaType.realClazz
      val field = try {
        clazz.getDeclaredField(name)
      } catch (e: NoSuchFieldException) {
        null
      }
      if (field != null) {
        return ClassField(JavaType.of(field.type), field.name, javaType, field.modifiers)
      }
      // try to find getter
      val methodFieldName = name.replaceFirstChar { it.uppercase() }
      val getterMethod  = findMethod(javaType, "get$methodFieldName", emptyList())
      val setterCandidates = getMarcelMethods(javaType).filter { it.name == "set$methodFieldName" && it.parameters.size  == 1}
      val setterMethod =
        if (setterCandidates.isEmpty()) findMethod(javaType, "set$methodFieldName", listOf(getterMethod?.returnType ?: JavaType.Object))
        else if (setterCandidates.size == 1) setterCandidates.first()
        else getMoreSpecificMethod(setterCandidates)
      if (getterMethod != null || setterMethod != null) {
        return MethodField.from(javaType, name, getterMethod, setterMethod)
      }
      return null
    } else {
      // TODO doesn't search on defined fields of notloaded type. Only search on super classes that are Loaded
      // searching on super types
      var type: JavaType? = javaType.superType!!
      while (type != null) {
        val f = findField(type, name, declared)
        if (f != null) return f
        type = type.superType
      }
      return null
    }
  }

  // ast node type resolver methods
  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) =
    findFieldOrThrow(getFieldAccessOperator.leftOperand.accept(this), getFieldAccessOperator.rightOperand.name).type

  override fun visit(literalListNode: LiteralArrayNode) = literalListNode.type ?: JavaType.arrayType(literalListNode.getElementsType(this))

  override fun visit(literalMapNode: LiteralMapNode) =
    JavaType.mapType(literalMapNode.getKeysType(this), literalMapNode.getValuesType(this))

  override fun visit(fCall: FunctionCallNode) = fCall.getMethod(this).returnType

}