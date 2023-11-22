package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.getElementsType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException
import com.tambapps.marcel.parser.scope.*
import com.tambapps.marcel.parser.type.*
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods
import org.objectweb.asm.Opcodes

open class JavaTypeResolver constructor(classLoader: MarcelClassLoader?) : AstNodeTypeResolver(classLoader) {

  constructor(): this(null)

  // extension methods or methods of marcel source code we're compiling
  private val marcelMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  private val fieldResolver = FieldResolver()

  init {
    loadDefaultExtensions()
  }
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

  override fun defineMethod(javaType: JavaType, method: JavaMethod) {
    val methods = getMarcelMethods(javaType)
    if (methods.any { it.matches(this, method.name, method.parameters) }) {
      throw MarcelSemanticLegacyException((method as? MethodNode)?.token, "Method with $method is already defined")
    }
    methods.add(method)
  }

  override fun defineField(javaType: JavaType, field: JavaField) {
    if (javaType.isLoaded) {
      throw MarcelSemanticLegacyException((field as? FieldNode)?.token, "Cannot define field on loaded class")
    }
    val marcelField = fieldResolver.computeFieldIfAbsent(javaType, field.name)
    marcelField.mergeWith(field)
  }

  override fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else marcelMethods[javaType.className] ?: emptyList()
  }

  override fun getClassField(javaType: JavaType, fieldName: String, node: AstNode?): ClassField {
    return fieldResolver.getField(javaType, fieldName)?.classField ?: throw MarcelSemanticLegacyException(node?.token, "Class field $javaType.$fieldName is not defined")
  }

  override fun getDeclaredFields(javaType: JavaType): Collection<MarcelField> {
    return fieldResolver.getAllFields(javaType).values
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

  override fun getMethods(javaType: JavaType): List<JavaMethod> {
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

  override fun doFindMethodByParameters(javaType: JavaType, name: String,
                                        positionalArgumentTypes: List<AstTypedObject>,
                                        namedParameters: Collection<MethodParameter>,
                                        excludeInterfaces: Boolean, node: AstNode?): JavaMethod? {
    return findMethod(javaType, name, { it.matchesUnorderedParameters(this, name, positionalArgumentTypes, namedParameters) },
      {candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      }, excludeInterfaces, node)
  }

  override fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean, node: AstNode?): JavaMethod? {
    var m = findMethod(javaType, name, { it.matches(this, name, argumentTypes) },
      {candidates ->  pickMethodCandidate(candidates, name, argumentTypes) }, excludeInterfaces, node)
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList(), false, node)
    }
    return m
  }

  private fun pickMethodCandidate(candidates: List<JavaMethod>, name: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    val exactCandidates = candidates.filter { it.exactMatch(name, argumentTypes) }
    return if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
  }

  override fun findMatchingMethod(methods: List<JavaMethod>, name: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    val candidates = methods.filter { it.matches(this, argumentTypes) }
    return pickMethodCandidate(candidates, name, argumentTypes)
  }

  private fun findMethod(javaType: JavaType, name: String,
                         matcherPredicate: (JavaMethod) -> Boolean,
                         candidatesPicker: (List<JavaMethod>) -> JavaMethod?,
                         excludeInterfaces: Boolean, node: AstNode?): JavaMethod? {
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

  override fun findField(javaType: JavaType, name: String, node: AstNode?): MarcelField? {
    if (javaType.isArray && (name == "size" || name == "length")) {
      return MarcelArrayLengthField(javaType, name)
    }
    val field = fieldResolver.getField(javaType, name)
    if (field == null && javaType.implements(JavaType.DynamicObject)) {
      return MarcelField(DynamicMethodField(javaType, name, JavaType.DynamicObject,
              ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("getProperty", String::class.java)),
              ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("setProperty", String::class.java, JavaType.DynamicObject.realClazz)),
              Opcodes.ACC_PUBLIC)
      )
    }
    return field
  }

  // ast node type resolver methods
  override fun visit(node: GetFieldAccessOperator): JavaType {
    val field = findFieldOrThrow(node.leftOperand.accept(this), node.rightOperand.name, node)
    if (node.directFieldAccess && field.classField == null) {
      throw MarcelSemanticLegacyException(node.token, "Class field ${node.scope.classType}.${node.rightOperand.name} is not defined")
    }
    val type = if (node.directFieldAccess) field.classField!!.type else field.type
    return if (node.nullSafe) type.objectType
    else type
  }

  override fun visit(node: GetIndexFieldAccessOperator): JavaType {
    val field = findFieldOrThrow(node.leftOperand.accept(this), node.rightOperand.name, node)
    if (node.directFieldAccess && field.classField == null) {
      throw MarcelSemanticLegacyException(node.token, "Class field ${node.scope.classType}.${node.rightOperand.name} is not defined")
    }

    val type = if (node.directFieldAccess) field.classField!!.type else field.type
    return if (type.isArray) type.asArrayType.elementsType
    else findMethodOrThrow(type, "getAt", node.rightOperand.indexArguments.map { it.accept(this) }, node).actualReturnType
  }

  override fun visit(node: LiteralArrayNode): JavaArrayType {
    if (node.type != null) return node.type!!
    val elementsType = node.getElementsType(this)
      ?: throw MarcelSemanticLegacyException(node.token, "Couldn't guess type of array. Use the 'as' keyword to explicitly specify the type")
    return JavaType.arrayType(elementsType)
  }

  override fun disposeClass(scriptNode: ClassNode) {
    super.disposeClass(scriptNode)
    marcelMethods.remove(scriptNode.type.className)
    fieldResolver.dispose(scriptNode.type.className)
    scriptNode.innerClasses.forEach { disposeClass(it) }
  }

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
            field.addGetter(MethodField(method.returnType, method.propertyName, method.ownerClass, method, null, method is ExtensionJavaMethod, method.access))
          } else if (method.isSetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { MarcelField(method.propertyName) }
            field.addSetter(MethodField(method.parameters.first().type, method.propertyName, method.ownerClass, null, method, method is ExtensionJavaMethod, method.access))
          }
        }
        return@computeIfAbsent fieldsMap
      }
    }

    private fun loadAllFields(javaType: JavaType): Set<MarcelField> {
      return if (javaType.isLoaded) (javaType.realClazz.fields + javaType.realClazz.declaredFields).map { MarcelField(ReflectJavaField(it)) }.toSet()
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