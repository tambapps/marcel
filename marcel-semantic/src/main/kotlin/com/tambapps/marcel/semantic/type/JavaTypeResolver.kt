package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.ExtensionJavaMethod
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.method.NoArgJavaConstructor
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.variable.field.ClassField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.JavaField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MethodField
import com.tambapps.marcel.semantic.method.ReflectJavaConstructor
import com.tambapps.marcel.semantic.variable.field.ReflectJavaField
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods

open class JavaTypeResolver constructor(private val classLoader: MarcelClassLoader?) {

  constructor(): this(null)

  private val _definedTypes = mutableMapOf<String, JavaType>()
  val definedTypes get() = _definedTypes.values.toList()

  // extension methods or methods of marcel source code we're compiling
  private val marcelMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  private val fieldResolver = FieldResolver()

  init {
    loadDefaultExtensions()
  }

  /* extensions */
  private fun loadDefaultExtensions() {
    loadExtension(DefaultMarcelMethods::class.javaType)
    loadExtensionsIfClassLoaded("FileExtensions", "StringMarcelMethods", "CharacterExtensions", "CharExtensions")
  }

  private fun loadExtensionsIfClassLoaded(vararg classNames: String) {
    classNames.forEach { loadExtensionIfClassLoaded(it) }
  }
  private fun loadExtensionIfClassLoaded(className: String) {
    val type = try {
      Class.forName("marcel.lang.extensions.$className").javaType
    } catch (e: ClassNotFoundException) {
      return
    }
    loadExtension(type)
  }

  fun loadExtension(vararg types: JavaType) {
    types.forEach { loadExtension(it) }
  }

  fun loadExtension(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() }
      .forEach {
        val owner = it.parameters.first().type
        defineMethod(owner, ExtensionJavaMethod(it))
      }
  }

  fun unloadExtension(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() }
      .forEach { extensionMethod ->
        val owner = extensionMethod.parameters.first().type
        val methods = getMarcelMethods(owner)
        methods.removeIf { it.matches(this, extensionMethod.name, extensionMethod.parameters) }

      }
  }

  /* definition */
  fun defineClass(token: LexToken? = null, visibility: Visibility, className: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    return defineClass(token, visibility, null, className, superClass, isInterface, interfaces)
  }
  fun defineClass(token: LexToken? = null, visibility: Visibility, outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName
    try {
      Class.forName(className)
      throw MarcelSemanticException(token, "Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (_definedTypes.containsKey(className)) throw MarcelSemanticException(token, "Class $className is already defined")
    val type = NotLoadedJavaType(visibility, className, emptyList(),  superClass, isInterface, interfaces.toMutableSet())
    _definedTypes[className] = type
    return type
  }

  fun defineMethod(javaType: JavaType, method: JavaMethod) {
    val methods = getMarcelMethods(javaType)
    if (methods.any { it.matches(this, method.name, method.parameters) }) {
      throw MarcelSemanticException((method as? MethodNode)?.token, "Method with $method is already defined")
    }
    methods.add(method)
  }

  fun defineField(javaType: JavaType, field: JavaField) {
    if (javaType.isLoaded) {
      throw MarcelSemanticException((field as? FieldNode)?.token, "Cannot define field on loaded class")
    }
    val marcelField = fieldResolver.computeFieldIfAbsent(javaType, field.name)
    marcelField.mergeWith(field)
  }

  fun registerClass(classNode: ClassNode) {
    _definedTypes[classNode.type.className] = classNode.type
 /* TODO
    classNode.methods.forEach { defineMethod(classNode.type, it) }
    classNode.fields.forEach { defineField(classNode.type, it) }
    classNode.innerClasses.forEach { registerClass(it) }

  */
  }

  fun isDefined(className: String): Boolean {
    return try {
      of(className, emptyList())
      true
    } catch (e: MarcelSemanticException) {
      false
    }
  }

  fun clear() {
    _definedTypes.clear()
  }

  fun of(className: String, genericTypes: List<JavaType>): JavaType {
    return _definedTypes[className] ?: JavaType.of(classLoader, className, genericTypes)
  }

  fun findMethodByParametersOrThrow(javaType: JavaType, name: String,
                                    positionalArgumentTypes: List<JavaTyped>,
                                    namedParameters: Collection<MethodParameter>, node: Ast2Node? = null): JavaMethod {
    return findMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, false, node)
      ?: throw MarcelSemanticException(node?.token, "Method $javaType.$name with parameters $namedParameters is not defined")
  }
  fun findMethodByParameters(javaType: JavaType, name: String,
                             positionalArgumentTypes: List<JavaTyped>,
                             namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false, node: Ast2Node? = null): JavaMethod? {
    val m = doFindMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, excludeInterfaces, node) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, node: Ast2Node? = null): JavaMethod {
    return findMethod(javaType, name, argumentTypes, false, node) ?: throw MarcelSemanticException(node?.token, "Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, excludeInterfaces: Boolean = false, node: Ast2Node? = null): JavaMethod? {
    val m = doFindMethod(javaType, name, argumentTypes, excludeInterfaces, node) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else marcelMethods[javaType.className] ?: emptyList()
  }

  fun getClassField(javaType: JavaType, fieldName: String, node: Ast2Node?): ClassField {
    return fieldResolver.getField(javaType, fieldName)?.classField ?: throw MarcelSemanticException(node?.token, "Class field $javaType.$fieldName is not defined")
  }

  fun getDeclaredFields(javaType: JavaType): Collection<MarcelField> {
    return fieldResolver.getAllFields(javaType).values
  }

  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod {
    return getDeclaredMethods(type).first { it.isAbstract }
  }

  private fun loadAllMethods(javaType: JavaType, excludeInterfaces: Boolean = false): Set<JavaMethod> {
    val methods = mutableSetOf<JavaMethod>()
    if (javaType.isLoaded) {
      javaType.realClazz.declaredMethods.forEach { methods.add(ReflectJavaMethod(it)) }
    }
    marcelMethods[javaType.className]?.let { methods.addAll(it) }

    var type = javaType.superType
    while (type != null) {
      methods.addAll(loadAllMethods(type, true))
      type = type.superType
    }
    if (!excludeInterfaces) {
      methods.addAll(javaType.allImplementedInterfaces.flatMap { loadAllMethods(it, true) })
    }
    return methods
  }

  fun getMethods(javaType: JavaType): List<JavaMethod> {
    if (javaType.isLoaded) return javaType.realClazz.methods.map { ReflectJavaMethod(it, javaType) }
    val methods = mutableListOf<JavaMethod>()
    var t: JavaType? = javaType
    while (t != null && !t.isLoaded) {
      methods.addAll(getDeclaredMethods(t))
      t = t.superType
    }
    // if it is not null at this point, then it must be a loaded type
    if (t != null) methods.addAll(getMethods(t))
    return methods
  }

  private fun doFindMethodByParameters(javaType: JavaType, name: String,
                                        positionalArgumentTypes: List<JavaTyped>,
                                        namedParameters: Collection<MethodParameter>,
                                        excludeInterfaces: Boolean, node: Ast2Node?): JavaMethod? {
    return findMethod(javaType, name, { it.matchesUnorderedParameters(this, name, positionalArgumentTypes, namedParameters) },
      {candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      }, excludeInterfaces, node)
  }

  private fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, excludeInterfaces: Boolean, node: Ast2Node?): JavaMethod? {
    var m = findMethod(javaType, name, { it.matches(this, name, argumentTypes) },
      {candidates ->  pickMethodCandidate(candidates, name, argumentTypes) }, excludeInterfaces, node)
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList(), false, node)
    }
    return m
  }

  private fun pickMethodCandidate(candidates: List<JavaMethod>, name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    val exactCandidates = candidates.filter { it.exactMatch(name, argumentTypes) }
    return if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
  }

  fun findMatchingMethod(methods: List<JavaMethod>, name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    val candidates = methods.filter { it.matches(this, argumentTypes) }
    return pickMethodCandidate(candidates, name, argumentTypes)
  }

  private fun findMethod(javaType: JavaType, name: String,
                         matcherPredicate: (JavaMethod) -> Boolean,
                         candidatesPicker: (List<JavaMethod>) -> JavaMethod?,
                         excludeInterfaces: Boolean, node: Ast2Node?): JavaMethod? {
    val methods = getMarcelMethods(javaType)
    var m = methods.find(matcherPredicate)
    if (m != null) return m

    if (javaType.isLoaded) {
      val clazz = javaType.type.realClazz
      val candidates = if (name == JavaMethod.CONSTRUCTOR_NAME) {
        clazz.declaredConstructors
          .map { ReflectJavaConstructor(it) }
          .filter(matcherPredicate)
      } else {
        clazz.declaredMethods
          .filter { it.name == name }
          .map { ReflectJavaMethod(it, javaType) }
          .filter(matcherPredicate)
      }
      m = candidatesPicker.invoke(candidates)
      if (m != null) return m
    }
    if (javaType.isArray) {
      // apparently Integer[] extends Number[] extends Object[] in some way (at least these are castable)
      val elementsType = javaType.asArrayType.elementsType.superType
      if (elementsType != null) {
        // recursive
        val candidate = findMethod(elementsType.arrayType, name, matcherPredicate, candidatesPicker, true, node)
        if (candidate != null) return candidate
      }
    }

    if (name == JavaMethod.CONSTRUCTOR_NAME) {
      if (!javaType.isLoaded) {
        val noArgConstructor = NoArgJavaConstructor(javaType)

        // if no constructors is explicitly defined for a marcel type, there is a default no arg constructor
        if (methods.count { it.isConstructor } == 0
          && matcherPredicate.invoke(noArgConstructor)) return noArgConstructor
      }
      // for constructors, we don't want to look in super types
      return null
    }
    // search in super types, but not for constructors
    var type = javaType.superType
    while (type != null) {
      m = findMethod(type, name, matcherPredicate, candidatesPicker, true, node)
      if (m != null) return m
      type = type.superType
    }

    if (excludeInterfaces) return null
    // now search on all implemented interfaces

    m = candidatesPicker.invoke(
      javaType.allImplementedInterfaces.mapNotNull {
        findMethod(it, name, matcherPredicate, candidatesPicker, false, node)
      }
    )
    return m
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
    return marcelMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  fun findField(javaType: JavaType, name: String): MarcelField? {
    if (javaType.isArray && (name == "size" || name == "length")) {
      return MarcelArrayLengthField(javaType, name)
    }
    val field = fieldResolver.getField(javaType, name)
    if (field == null && javaType.implements(JavaType.DynamicObject)) {
      return MarcelField(
        DynamicMethodField(javaType, name, JavaType.DynamicObject,
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("getProperty", String::class.java)),
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("setProperty", String::class.java, JavaType.DynamicObject.realClazz)))
      )
    }
    return field
  }

  /* resolve types */
  private inner class FieldResolver {
    private val classFields = mutableMapOf<String, MutableMap<String, MarcelField>>()

    // map fieldName -> MarcelField
    fun getField(javaType: JavaType, fieldName: String): MarcelField? {
      return getAllFields(javaType)[fieldName]
    }

    fun computeFieldIfAbsent(javaType: JavaType, fieldName: String): MarcelField {
      return getAllFields(javaType).computeIfAbsent(fieldName) { MarcelField(fieldName) }
    }

    fun getAllFields(javaType: JavaType): MutableMap<String, MarcelField> {
      return classFields.computeIfAbsent(javaType.className) {
        val directFields = loadAllFields(javaType)

        val fieldsMap = directFields.associateBy { it.name }.toMutableMap()

        val methods = loadAllMethods(javaType)
        for (method in methods) {
          if (method.isGetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { MarcelField(method.propertyName) }
            field.addGetter(MethodField(method.returnType, method.propertyName, method.ownerClass, method, null, method is ExtensionJavaMethod))
          } else if (method.isSetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { MarcelField(method.propertyName) }
            field.addSetter(MethodField(method.parameters.first().type, method.propertyName, method.ownerClass, null, method, method is ExtensionJavaMethod))
          }
        }
        return@computeIfAbsent fieldsMap
      }
    }

    private fun loadAllFields(javaType: JavaType): Set<MarcelField> {
      return if (javaType.isLoaded) (javaType.realClazz.fields + javaType.realClazz.declaredFields).map { MarcelField(
        ReflectJavaField(it)
      ) }.toSet()
      else {
        val fieldsMap = classFields[javaType.className]?.values?.associateBy { it.name }?.toMutableMap() ?: mutableMapOf()
        var type = javaType.superType
        while (type != null) {
          val fields = loadAllFields(type)
          fields.forEach {
            if (fieldsMap.containsKey(it.name)) {
              fieldsMap.getValue(it.name).mergeWith(it)
            } else fieldsMap[it.name] = it
          }
          if (type.isLoaded) break
          type = type.superType
        }
        return fieldsMap.values.toSet()
      }
    }

    fun dispose(className: String) {
      classFields.remove(className)
    }
  }

}