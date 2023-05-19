package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.getElementsType
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getValuesType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.scope.ReflectMarcelField
import com.tambapps.marcel.parser.type.ExtensionJavaMethod
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.NoArgJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods

open class JavaTypeResolver constructor(classLoader: MarcelClassLoader?) : AstNodeTypeResolver(classLoader) {

  constructor(): this(null)

  private val classMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  private val classFields = mutableMapOf<String, MutableList<MarcelField>>()

  init {
    loadDefaultExtensions()
  }
  private fun loadDefaultExtensions() {
    loadExtension(DefaultMarcelMethods::class.javaType)
    loadExtensionsIfClassLoaded("IoMarcelMethods", "StringMarcelMethods", "CharacterExtensions", "CharExtensions")
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
      throw MarcelSemanticException("Method with $method is already defined")
    }
    methods.add(method)
  }

  override fun defineField(javaType: JavaType, field: MarcelField) {
    if (javaType.isLoaded) throw MarcelSemanticException("Cannot define field on loaded class")
    val fields = getMarcelFields(javaType)
    fields.add(field)
  }

  override fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else classMethods[javaType.className] ?: emptyList()
  }

  override fun getDeclaredFields(javaType: JavaType): List<MarcelField> {
    return if (javaType.isLoaded) javaType.realClazz.declaredFields.map { ReflectMarcelField(it) }
    else classFields[javaType.className] ?: emptyList()
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

  override fun getFields(javaType: JavaType): List<MarcelField> {
    if (javaType.isLoaded) return javaType.realClazz.fields.map { ReflectMarcelField(it) }
    val fields = mutableListOf<MarcelField>()
    var t: JavaType? = javaType
    while (t != null && !t.isLoaded) {
      fields.addAll(getDeclaredFields(t))
      t = t.superType
    }
    // if it is not null at this point, then it must be a loaded type
    if (t != null) fields.addAll(getFields(t))
    return fields
  }

  override fun doFindMethodByParameters(javaType: JavaType, name: String,
                                        positionalArgumentTypes: List<AstTypedObject>,
                                        namedParameters: Collection<MethodParameter>,
                                        excludeInterfaces: Boolean): JavaMethod? {
    return findMethod(javaType, name, { it.matchesUnorderedParameters(this, name, positionalArgumentTypes, namedParameters) },
      {candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else null// returning null because we wan't to get more specific method
      }, excludeInterfaces)
  }

  override fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean): JavaMethod? {
    var m = findMethod(javaType, name, { it.matches(this, name, argumentTypes) },
      {candidates ->
        val exactCandidates = candidates.filter { it.exactMatch(name, argumentTypes) }
        if (exactCandidates.size == 1) exactCandidates.first() else null// returning null because we wan't to get more specific method

      }, excludeInterfaces)
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList())
    }
    return m
  }

  private fun findMethod(javaType: JavaType, name: String,
                         matcherPredicate: (JavaMethod) -> Boolean,
                         candidatesPicker: (List<JavaMethod>) -> JavaMethod?,
                         excludeInterfaces: Boolean): JavaMethod? {
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
      m = candidatesPicker.invoke(candidates)  ?: getMoreSpecificMethod(candidates)
      if (m != null) return m
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
      m = findMethod(type, name, matcherPredicate, candidatesPicker, true)
      if (m != null) return m
      type = type.superType
    }

    if (excludeInterfaces) return null
    // now search on all implemented interfaces
    for (interfaze in javaType.allImplementedInterfaces) {
      m = findMethod(interfaze, name, matcherPredicate, candidatesPicker, false)
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

  private fun getMarcelFields(javaType: JavaType): MutableList<MarcelField> {
    return classFields.computeIfAbsent(javaType.className) { mutableListOf() }
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
    } else {
      val fields = getMarcelFields(javaType)
      val field = fields.find { it.name == name }
      if (field != null) return field

      // searching on super types
      var type: JavaType? = javaType.superType!!
      while (type != null) {
        val f = findField(type, name, declared)
        if (f != null) return f
        if (type.isLoaded) break // in loaded classes, we already handle super types so no need to go further
        type = type.superType
      }
    }

    // try to find a method field
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
  }

  // ast node type resolver methods
  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) =
    if (getFieldAccessOperator.nullSafe) findFieldOrThrow(getFieldAccessOperator.leftOperand.accept(this), getFieldAccessOperator.rightOperand.name).type.objectType
    else findFieldOrThrow(getFieldAccessOperator.leftOperand.accept(this), getFieldAccessOperator.rightOperand.name).type

  override fun visit(literalListNode: LiteralArrayNode): JavaArrayType {
    if (literalListNode.type != null) return literalListNode.type!!
    val elementsType = literalListNode.getElementsType(this)
      ?: throw MarcelSemanticException(literalListNode.token, "Couldn't guess type of array. Use the 'as' keyword to explicitly specify the type")
    return JavaType.arrayType(elementsType)
  }

  override fun visit(literalMapNode: LiteralMapNode) =
    JavaType.mapType(literalMapNode.getKeysType(this), literalMapNode.getValuesType(this))

  override fun visit(fCall: FunctionCallNode) = fCall.getMethod(this).actualReturnType
  override fun disposeClass(scriptNode: ClassNode) {
    super.disposeClass(scriptNode)
    classMethods.remove(scriptNode.type.className)
    classFields.remove(scriptNode.type.className)
    scriptNode.innerClasses.forEach { disposeClass(it) }
  }

}