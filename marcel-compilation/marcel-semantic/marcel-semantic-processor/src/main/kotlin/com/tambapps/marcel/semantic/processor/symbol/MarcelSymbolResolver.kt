package com.tambapps.marcel.semantic.processor.symbol

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.exception.TypeNotFoundException
import com.tambapps.marcel.semantic.processor.exception.VariableNotFoundException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.CastMethod
import com.tambapps.marcel.semantic.method.ExtensionMarcelMethod
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.method.MethodMatcherTrait
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.method.NoArgJavaConstructor
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import com.tambapps.marcel.semantic.variable.field.MethodField
import com.tambapps.marcel.semantic.method.ReflectJavaConstructor
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.PRIMITIVE_LIST_MAP
import com.tambapps.marcel.semantic.type.JavaType.Companion.PRIMITIVE_SET_MAP
import com.tambapps.marcel.semantic.type.JavaTyped
import com.tambapps.marcel.semantic.type.SourceJavaType
import com.tambapps.marcel.semantic.variable.field.ReflectJavaField
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods

/**
 * Class allowing to define and resolve all symbols (classes, methods, fields) of a Marcel AST
 *
 * @property classLoader an optional class loader
 */
open class MarcelSymbolResolver(private val classLoader: MarcelClassLoader?) : MethodMatcherTrait,
  SymbolDefinerTrait {

  companion object {
    private val PRIMITIVE_COLLECTION_TYPE_MAP = mapOf(
      Pair("List", PRIMITIVE_LIST_MAP),
      Pair("Set", PRIMITIVE_SET_MAP),
    )
  }

  constructor() : this(null)

  private val _definedTypes = mutableMapOf<String, JavaType>()
  val definedTypes get() = _definedTypes.values.toList()

  // extension methods or methods of marcel source code we're compiling
  private val marcelMethods = mutableMapOf<String, MutableList<MarcelMethod>>()
  private val fieldResolver = FieldResolver()

  init {
    loadDefaultExtensions()
    loadCustomMethods()
  }

  /* extensions */
  private fun loadDefaultExtensions() {
    loadExtensionUnsafe(DefaultMarcelMethods::class.javaType)
    loadExtensionsUnsafeIfClassLoaded("FileExtensions", "CharSequenceExtensions", "CharacterExtensions", "CharExtensions")
  }

  private fun loadCustomMethods() {
    val methods = marcelMethods.computeIfAbsent(JavaType.Object.className) { mutableListOf() }
    methods.addAll(CastMethod.METHODS)
  }

  private fun loadExtensionsUnsafeIfClassLoaded(vararg classNames: String) {
    classNames.forEach { loadExtensionUnsafeIfClassLoaded(it) }
  }

  private fun loadExtensionUnsafeIfClassLoaded(className: String) {
    val type = try {
      Class.forName("marcel.lang.extensions.$className").javaType
    } catch (e: ClassNotFoundException) {
      return
    }
    loadExtensionUnsafe(type)
  }

  fun loadExtension(vararg types: JavaType) {
    types.forEach { loadExtension(it) }
  }

  fun loadExtension(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() && it.visibility != Visibility.PRIVATE }
      .forEach {
        val owner = it.parameters.first().type
        defineExtensionMethod(owner, it)
      }
  }


  // Not checking if method is already defined. will allow going faster
  protected fun loadExtensionUnsafe(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() && it.visibility != Visibility.PRIVATE }
      .forEach {
        // extensions can be from a non-extension class, which can extend many types (see DefaultMarcelMethods)
        defineExtensionMethodUnsafe(it)
      }
  }

  fun unloadExtension(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() && it.visibility != Visibility.PRIVATE }
      .forEach { originalMethod ->
        val extensionMethod = toExtensionMethod(originalMethod)
        val methods = getMarcelMethods(extensionMethod.marcelOwnerClass)
        methods.removeIf { strictMatch(it, extensionMethod.name, extensionMethod.parameters) }
      }
  }

  /* definition */
  fun defineType(
    token: LexToken = LexToken.DUMMY,
    visibility: Visibility,
    className: String,
    superClass: JavaType,
    isInterface: Boolean,
    interfaces: List<JavaType>,
    isFunctionalInterface: Boolean = false,
    isScript: Boolean = false,
    isEnum: Boolean = false,
    isFinal: Boolean = false,
    isAbstract: Boolean = false,
    isAnnotation: Boolean = false,
    isExtensionType: Boolean = false,
    extendedType: JavaType? = null
  ): SourceJavaType {
    checkTypeAlreadyDefined(token, className)
    val type = SourceJavaType(
      visibility,
      className,
      emptyList(),
      superClass,
      isInterface = isInterface,
      isFunctionalInterface = isFunctionalInterface,
      directlyImplementedInterfaces = interfaces.toMutableSet(),
      isScript = isScript,
      isEnum = isEnum,
      isFinal = isFinal,
      isAbstract = isAbstract,
      isAnnotation = isAnnotation,
      isExtensionType = isExtensionType,
      globalExtendedType = extendedType
    )
    _definedTypes[className] = type
    return type
  }

  override fun defineType(token: LexToken, javaType: JavaType) {
    checkTypeAlreadyDefined(token, javaType.className)
    _definedTypes[javaType.className] = javaType
  }

  private fun checkTypeAlreadyDefined(token: LexToken, className: String) {
    try {
      Class.forName(className)
      throw MarcelSemanticException(token, "Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (_definedTypes.containsKey(className)) throw MarcelSemanticException(
      token,
      "Class $className is already defined"
    )

  }

  // TODO need to test these
  fun defineExtensionMethod(methodOwner: JavaType, originalMethod: MarcelMethod) {
    val extensionMethod = toExtensionMethod(originalMethod)
    defineMethod(methodOwner, extensionMethod)
  }

  private fun toExtensionMethod(originalMethod: MarcelMethod): ExtensionMarcelMethod {
    if (!originalMethod.ownerClass.isExtensionType) {
      throw MarcelSemanticException(
        (originalMethod as? MethodNode)?.token ?: LexToken.DUMMY,
        "Type ${originalMethod.ownerClass} is not an extension")
    }
    val extendedType = originalMethod.ownerClass.globalExtendedType ?: originalMethod.parameters.firstOrNull()
      ?.takeIf { it.name == ExtensionMarcelMethod.THIS_PARAMETER_NAME }?.type
    if (extendedType == null) {
      throw MarcelSemanticException(LexToken.DUMMY, "Invalid extension method $originalMethod: couldn't retrieve owner type")
    }
    return ExtensionMarcelMethod.toExtension(originalMethod, extendedType)
  }

  private fun defineExtensionMethodUnsafe(originalMethod: MarcelMethod) {
    val extensionMethod = ExtensionMarcelMethod.toExtension(originalMethod,
      // some extension class like DefaultMarcelMethods are actually extension class without the @ExtensionClass class annotations, that provide extension for many types.
      // that's why we fall back to the first parameter type
      originalMethod.ownerClass.globalExtendedType ?: originalMethod.parameters.first().type)
    defineMethodUnsafe(extensionMethod.marcelOwnerClass, extensionMethod)
  }

  fun defineMethod(javaType: JavaType, method: MarcelMethod) {
    val methods = getMarcelMethods(javaType)
    if (methods.any { it.matches(method) }) {
      throw MarcelSemanticException(
        (method as? MethodNode)?.token ?: LexToken.DUMMY,
        "Method $method conflicts with method " + methods.find { it.matches(method) })
    }
    defineMethodUnsafe(javaType, method)
  }

  private fun defineMethodUnsafe(javaType: JavaType, method: MarcelMethod) {
    val methods = getMarcelMethods(javaType)
    methods.add(method)
    if (method.isGetter || method.isSetter) {
      fieldResolver.defineMethodField(method)
    }
  }

  fun undefineMethod(javaType: JavaType, method: MarcelMethod) {
    getMarcelMethods(javaType).remove(method)
    if (method.isGetter || method.isSetter) {
      fieldResolver.undefineMethodField(method)
    }
  }

  fun defineField(field: MarcelField) = defineField(field.owner, field)

  open fun defineField(javaType: JavaType, field: MarcelField) {
    if (javaType.isLoaded && field is JavaClassField) {
      throw MarcelSemanticException(
        (field as? FieldNode)?.token ?: LexToken.DUMMY,
        "Cannot define field on loaded class"
      )
    }
    val marcelField = fieldResolver.computeFieldIfAbsent(javaType, field.name)
    marcelField.mergeWith(field)
  }

  fun defineType(classNode: ClassNode) {
    _definedTypes[classNode.type.className] = classNode.type
    classNode.methods.forEach { defineMethod(classNode.type, it) }
    classNode.fields.forEach { defineField(classNode.type, it) }
    classNode.innerClasses.forEach { defineType(it) }
  }

  fun undefineClass(scriptNode: ClassNode) {
    _definedTypes.remove(scriptNode.type.className)
    marcelMethods.remove(scriptNode.type.className)
    fieldResolver.dispose(scriptNode.type.className)
    scriptNode.innerClasses.forEach { undefineClass(it) }
  }

  fun isDefined(className: String): Boolean {
    return try {
      of(className, emptyList(), LexToken.DUMMY)
      true
    } catch (e: MarcelSemanticException) {
      false
    }
  }

  fun of(className: String, genericTypes: List<JavaType> = emptyList()) = of(className, genericTypes, LexToken.DUMMY)
  fun of(className: String, genericTypes: List<JavaType> = emptyList(), token: LexToken): JavaType {
    if (_definedTypes.containsKey(className)) return _definedTypes.getValue(className)
    val optPrimitiveType = JavaType.PRIMITIVES.find { it.className == className }
    if (optPrimitiveType != null) return optPrimitiveType
    val optArrayType = JavaType.ARRAYS.find { it.className == className }
    if (optArrayType != null) return optArrayType

    if (genericTypes.size == 1) {
      val type = PRIMITIVE_COLLECTION_TYPE_MAP[className]?.get(genericTypes.first())
      if (type != null) return type
    }
    try {
      val clazz = if (classLoader != null) classLoader.loadClass(className)
      else Class.forName(className)
      return JavaType.of(clazz).withGenericTypes(genericTypes)
    } catch (e: ClassNotFoundException) {
      throw TypeNotFoundException(token, "Class $className was not found")
    }
  }

  fun clear() {
    _definedTypes.clear()
  }

  fun findMethodByParametersOrThrow(
    javaType: JavaType, name: String,
    positionalArgumentTypes: List<JavaTyped>,
    namedParameters: Collection<MethodParameter>, token: LexToken = LexToken.DUMMY
  ): MarcelMethod {
    return findMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, false, token)
      ?: throw MarcelSemanticException(token, "Method $javaType.$name with parameters $namedParameters is not defined")
  }

  fun findMethodByParameters(
    javaType: JavaType, name: String,
    positionalArgumentTypes: List<JavaTyped>,
    namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false, token: LexToken = LexToken.DUMMY
  ): MarcelMethod? {
    val m = doFindMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, excludeInterfaces, token)
      ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun findMethodOrThrow(
    javaType: JavaType,
    name: String,
    argumentTypes: List<JavaTyped>,
    token: LexToken = LexToken.DUMMY
  ): MarcelMethod {
    return findMethod(javaType, name, argumentTypes, false, token) ?: throw MarcelSemanticException(
      token,
      "Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined"
    )
  }

  fun findSuperMethod(method: MarcelMethod): MarcelMethod? {
    return method.ownerClass.directlyImplementedInterfaces.firstNotNullOfOrNull { findMethod(it, method.name, method.parameters) }
      ?: method.ownerClass.superType?.let { findMethod(it, method.name, method.parameters) }
  }

  fun findConstructor(
    javaType: JavaType,
    argumentTypes: List<JavaTyped>,
    excludeInterfaces: Boolean = false,
    token: LexToken = LexToken.DUMMY
  ) = findMethod(javaType, MarcelMethod.CONSTRUCTOR_NAME, argumentTypes, excludeInterfaces, token)

  fun findMethod(
    javaType: JavaType,
    name: String,
    argumentTypes: List<JavaTyped>,
    excludeInterfaces: Boolean = false,
    token: LexToken = LexToken.DUMMY
  ): MarcelMethod? {
    val m = doFindMethod(javaType, name, argumentTypes, excludeInterfaces, token) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun getDeclaredMethods(javaType: JavaType): List<MarcelMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else marcelMethods[javaType.className] ?: emptyList()
  }

  fun getClassField(javaType: JavaType, fieldName: String, token: LexToken = LexToken.DUMMY): JavaClassField {
    return fieldResolver.getField(javaType, fieldName)?.classField ?: throw MarcelSemanticException(
      token,
      "Class field $javaType.$fieldName is not defined"
    )
  }

  fun undefineField(javaType: JavaType, fieldName: String) = fieldResolver.undefineField(javaType, fieldName)

  fun undefineField(field: MarcelField) = fieldResolver.undefineField(field.owner, field.name)

  fun getDeclaredFields(javaType: JavaType): Collection<CompositeField> {
    return fieldResolver.getAllFields(javaType).values
  }

  fun getInterfaceLambdaMethodOrThrow(type: JavaType, token: LexToken): MarcelMethod {
    return getInterfaceLambdaMethod(type) ?: throw MarcelSemanticException(
      token,
      "Interface isn't a functional interface"
    )
  }

  override fun getInterfaceLambdaMethod(type: JavaType): MarcelMethod? {
    return getDeclaredMethods(type).firstOrNull { it.isAbstract && it.name != "equals" && it.name != "hashCode" }
  }

  private fun loadAllMethods(javaType: JavaType, excludeInterfaces: Boolean = false): Set<MarcelMethod> {
    val methods = mutableSetOf<MarcelMethod>()
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

  fun getMethods(javaType: JavaType): List<MarcelMethod> {
    if (javaType.isLoaded) return javaType.realClazz.methods.map {
      val method = ReflectJavaMethod(it, javaType)
      if (javaType.isExtensionType && method.isStatic && method.parameters.isNotEmpty() && method.visibility != Visibility.PRIVATE) {
        toExtensionMethod(method)
      } else {
        method
      }
    }
    val methods = mutableListOf<MarcelMethod>()
    var t: JavaType? = javaType
    while (t != null && !t.isLoaded) {
      methods.addAll(getDeclaredMethods(t))
      t = t.superType
    }
    // if it is not null at this point, then it must be a loaded type
    if (t != null) methods.addAll(getMethods(t))
    return methods
  }

  private fun doFindMethodByParameters(
    javaType: JavaType, name: String,
    positionalArgumentTypes: List<JavaTyped>,
    namedParameters: Collection<MethodParameter>,
    excludeInterfaces: Boolean, token: LexToken? = null
  ): MarcelMethod? {
    return findMethod(javaType,
      name,
      { matchesUnorderedParameters(it, name, positionalArgumentTypes, namedParameters) },
      { candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      },
      excludeInterfaces,
      token
    )
  }

  private fun doFindMethod(
    javaType: JavaType,
    name: String,
    argumentTypes: List<JavaTyped>,
    excludeInterfaces: Boolean,
    token: LexToken = LexToken.DUMMY
  ): MarcelMethod? {
    var m = findMethod(javaType, name, { matches(it, name, argumentTypes) },
      { candidates -> pickMethodCandidate(candidates, name, argumentTypes) }, excludeInterfaces, token
    )
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList(), false, token)
    }
    return m
  }


  private fun pickMethodCandidate(
    candidates: List<MarcelMethod>,
    name: String,
    argumentTypes: List<JavaTyped>
  ): MarcelMethod? {
    val exactCandidates = candidates.filter { strictMatch(it, name, argumentTypes) }
    return if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(
      if (exactCandidates.isNotEmpty()) exactCandidates else candidates
    )
  }

  fun findMatchingMethod(methods: List<MarcelMethod>, name: String, argumentTypes: List<JavaTyped>): MarcelMethod? {
    val candidates = methods.filter { matches(it, argumentTypes) }
    return pickMethodCandidate(candidates, name, argumentTypes)
  }

  private fun findMethod(
    javaType: JavaType, name: String,
    matcherPredicate: (MarcelMethod) -> Boolean,
    candidatesPicker: (List<MarcelMethod>) -> MarcelMethod?,
    excludeInterfaces: Boolean, token: LexToken? = null
  ): MarcelMethod? {
    val methods = getMarcelMethods(javaType)
    var m = candidatesPicker.invoke(methods.filter(matcherPredicate))
    if (m != null) return m

    if (javaType.isLoaded) {
      val clazz = javaType.type.realClazz
      val candidates = if (name == MarcelMethod.CONSTRUCTOR_NAME) {
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
        val candidate = findMethod(elementsType.arrayType, name, matcherPredicate, candidatesPicker, true, token)
        if (candidate != null) return candidate
      }
    }

    if (name == MarcelMethod.CONSTRUCTOR_NAME) {
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
    var type = if (!javaType.isInterface) javaType.superType else JavaType.Object
    while (type != null) {
      m = findMethod(type, name, matcherPredicate, candidatesPicker, true, token)
      if (m != null) return m
      type = type.superType
    }

    if (excludeInterfaces) return null
    // now search on all implemented interfaces

    val candidates = javaType.allImplementedInterfaces.mapNotNull {
      findMethod(it, name, matcherPredicate, candidatesPicker, false, token)
    }

    m = candidatesPicker.invoke(candidates)
    return m
  }

  private fun getMoreSpecificMethod(candidates: List<MarcelMethod>): MarcelMethod? {
    val nonExtensionMethods = candidates.filter { !it.isExtension }
    val toIterate = nonExtensionMethods.ifEmpty { candidates }

    // inspired from Class.searchMethods()
    var m: MarcelMethod? = null
    for (candidate in toIterate) {
      if (m == null
        || (m.returnType != candidate.returnType
            && m.returnType.isAssignableFrom(candidate.returnType))
      ) m = candidate
    }
    return m
  }

  private fun getMarcelMethods(javaType: JavaType): MutableList<MarcelMethod> {
    // return methods defined from MDK or from marcel source we're currently compiling
    return marcelMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  fun findFieldOrThrow(javaType: JavaType, name: String, token: LexToken = LexToken.DUMMY): CompositeField {
    return findField(javaType, name) ?: throw VariableNotFoundException(token, "Field $javaType.$name is not defined")
  }

  open fun findField(javaType: JavaType, name: String): CompositeField? {
    if (javaType.isArray && (name == "size" || name == "length")) {
      return CompositeField(MarcelArrayLengthField(javaType, name))
    }
    val field = fieldResolver.getField(javaType, name)
    if (field == null && javaType.implements(JavaType.DynamicObject)) {
      return CompositeField(
        DynamicMethodField(
          javaType, name, JavaType.DynamicObject,
          ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("getProperty", String::class.java)),
          ReflectJavaMethod(
            DynamicObject::class.java.getDeclaredMethod(
              "setProperty",
              String::class.java,
              JavaType.DynamicObject.realClazz
            )
          )
        )
      )
    }
    return field
  }

  /* resolve types */
  private inner class FieldResolver {
    private val classFields = mutableMapOf<String, MutableMap<String, CompositeField>>()

    // map fieldName -> MarcelField
    fun getField(javaType: JavaType, fieldName: String): CompositeField? {
      return getAllFields(javaType)[fieldName]
    }

    fun computeFieldIfAbsent(javaType: JavaType, fieldName: String): CompositeField {
      return getAllFields(javaType).computeIfAbsent(fieldName) { CompositeField(fieldName) }
    }

    fun getAllFields(javaType: JavaType): MutableMap<String, CompositeField> {
      return classFields.computeIfAbsent(javaType.className) {
        val directFields = loadAllFields(javaType)

        val fieldsMap = directFields.associateBy { it.name }.toMutableMap()

        val methods = loadAllMethods(javaType)
        for (method in methods) {
          if (method.isGetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { CompositeField(method.propertyName) }
            field.addGetter(MethodField.fromGetter(method))
          } else if (method.isSetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { CompositeField(method.propertyName) }
            field.addSetter(MethodField.fromSetter(method))
          }
        }
        return@computeIfAbsent fieldsMap
      }
    }

    fun defineMethodField(method: MarcelMethod) {
      val compositeField = computeFieldIfAbsent(method.ownerClass, method.propertyName)
      if (method.isGetter) {
        compositeField.addGetter(MethodField.fromGetter(method))
      } else if (method.isSetter) {
        compositeField.addSetter(MethodField.fromSetter(method))
      }
    }

    fun undefineMethodField(method: MarcelMethod) {
      val compositeField = computeFieldIfAbsent(method.ownerClass, method.propertyName)
      if (method.isGetter) {
        compositeField.removeGetter(MethodField.fromGetter(method))
      } else if (method.isSetter) {
        compositeField.removeSetter(MethodField.fromSetter(method))
      }
      if (compositeField.isEmpty()) {
        getAllFields(method.ownerClass).remove(method.propertyName)
      }
    }

    fun undefineField(javaType: JavaType, name: String) = getAllFields(javaType).remove(name)

    private fun loadAllFields(javaType: JavaType): Set<CompositeField> {
      return if (javaType.isLoaded) (javaType.realClazz.fields + javaType.realClazz.declaredFields).map {
        CompositeField(
          ReflectJavaField(it)
        )
      }.toSet()
      else {
        val fieldsMap =
          classFields[javaType.className]?.values?.associateBy { it.name }?.toMutableMap() ?: mutableMapOf()
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