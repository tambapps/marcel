package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod

class JavaTypeResolver {

  private val classMethods = mutableMapOf<String, MutableList<JavaMethod>>()


  fun defineMethod(javaType: JavaType, method: JavaMethod) {
    // TODO defining in both durnig transition
    javaType.defineMethod(method)
    val methods = getTypeMethods(javaType)
    if (methods.any { it.matches(method.name, method.parameters) }) {
      throw SemanticException("Method with $method is already defined")
    }
    methods.add(method)
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    return findMethod(javaType, name, argumentTypes) ?: throw SemanticException("Method $this.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false): JavaMethod? {
    val methods = getTypeMethods(javaType)
    var m = methods.find { it.matches(name, argumentTypes) }
    if (m != null) return m

    if (javaType.isLoaded) {
      val clazz = javaType.type.realClazz
      val candidates = if (name == JavaMethod.CONSTRUCTOR_NAME) {
        clazz.declaredConstructors
          .map { ReflectJavaConstructor(it) }
          .filter { it.matches(argumentTypes) }
      } else {
        clazz.declaredMethods
          .filter { it.name == name }
          .map { ReflectJavaMethod(it, javaType) }
          .filter { it.matches(argumentTypes) }
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

  private fun getTypeMethods(javaType: JavaType): MutableList<JavaMethod> {
    return classMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  fun findFieldOrThrow(javaType: JavaType, name: String, declared: Boolean = true): MarcelField {
    return findField(javaType, name, declared) ?: throw SemanticException("Field $name was not found")
  }

  fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
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
      val setterMethod = findMethod(javaType, "set$methodFieldName", listOf(javaType))
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
        type = if (type.superType != null) JavaType.of(type.superType?.className!!) else null
      }
      return null
    }
  }
}