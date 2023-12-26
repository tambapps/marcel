package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
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
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import com.tambapps.marcel.semantic.variable.field.MethodField
import com.tambapps.marcel.semantic.method.ReflectJavaConstructor
import com.tambapps.marcel.semantic.variable.field.ReflectJavaField
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods
import kotlin.math.max

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
    loadExtensionUnsafe(DefaultMarcelMethods::class.javaType)
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
    loadExtensionUnsafe(type)
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


  // Not checking if method is already defined. will allow going faster
  private fun loadExtensionUnsafe(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() }
      .forEach {
        val owner = it.parameters.first().type
        val methods = getMarcelMethods(owner)
        methods.add(ExtensionJavaMethod(it))
      }
  }

  fun unloadExtension(type: JavaType) {
    getDeclaredMethods(type).filter { it.isStatic && it.parameters.isNotEmpty() }
      .forEach { extensionMethod ->
        val owner = extensionMethod.parameters.first().type
        val methods = getMarcelMethods(owner)
        methods.removeIf { matches(it, extensionMethod.name, extensionMethod.parameters) }

      }
  }

  /* definition */
  fun defineClass(token: LexToken? = null, visibility: Visibility, className: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>, isScript: Boolean = false): NotLoadedJavaType {
    return defineClass(token, visibility, null, className, superClass, isInterface, interfaces, isScript)
  }
  fun defineClass(token: LexToken? = null, visibility: Visibility, outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>, isScript: Boolean = false): NotLoadedJavaType {
    val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName

    checkTypeAlreadyDefined(token, className)
    val type = NotLoadedJavaType(visibility, className, emptyList(),  superClass, isInterface, interfaces.toMutableSet(), isScript = isScript)
    _definedTypes[className] = type
    return type
  }

  internal fun defineType(token: LexToken? = null, javaType: JavaType) {
    checkTypeAlreadyDefined(token, javaType.className)
    _definedTypes[javaType.className] = javaType
  }

  private fun checkTypeAlreadyDefined(token: LexToken? = null, className: String) {
    try {
      Class.forName(className)
      throw MarcelSemanticException(token, "Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (_definedTypes.containsKey(className)) throw MarcelSemanticException(token, "Class $className is already defined")

  }
  fun defineMethod(javaType: JavaType, method: JavaMethod) {
    val methods = getMarcelMethods(javaType)
    if (methods.any { matches(it, method.name, method.parameters, strict = true) }) {
      throw MarcelSemanticException((method as? MethodNode)?.token ?: LexToken.DUMMY, "Method with $method is already defined")
    }
    methods.add(method)
  }

  fun undefineMethod(javaType: JavaType, method: JavaMethod): Boolean {
    return getMarcelMethods(javaType).remove(method)
  }

  open fun defineField(javaType: JavaType, field: MarcelField) {
    if (javaType.isLoaded) {
      throw MarcelSemanticException((field as? FieldNode)?.token, "Cannot define field on loaded class")
    }
    val marcelField = fieldResolver.computeFieldIfAbsent(javaType, field.name)
    marcelField.mergeWith(field)
  }

  fun defineClass(classNode: ClassNode) {
    _definedTypes[classNode.type.className] = classNode.type
    classNode.methods.forEach { defineMethod(classNode.type, it) }
    classNode.fields.forEach { defineField(classNode.type, it) }
    classNode.innerClasses.forEach { defineClass(it) }
  }

  fun undefineClass(scriptNode: ClassNode) {
    _definedTypes.remove(scriptNode.type.className)
    marcelMethods.remove(scriptNode.type.className)
    fieldResolver.dispose(scriptNode.type.className)
    scriptNode.innerClasses.forEach { undefineClass(it) }
  }

  fun isDefined(className: String): Boolean {
    return try {
      of(LexToken.DUMMY, className, emptyList())
      true
    } catch (e: MarcelSemanticException) {
      false
    }
  }

  fun of(className: String, genericTypes: List<JavaType> = emptyList()) = of(LexToken.DUMMY, className, genericTypes)
  fun of(token: LexToken, className: String, genericTypes: List<JavaType> = emptyList()): JavaType {
    if (_definedTypes.containsKey(className)) return _definedTypes.getValue(className)
    val optPrimitiveType = JavaType.PRIMITIVES.find { it.className == className }
    if (optPrimitiveType != null) return optPrimitiveType
    val optArrayType = JavaType.ARRAYS.find { it.className == className }
    if (optArrayType != null) return optArrayType

    if (genericTypes.size == 1) {
      val type = JavaType.PRIMITIVE_COLLECTION_TYPE_MAP[className]?.get(genericTypes.first())
      if (type != null) return type
    }
    try {
      val clazz = if (classLoader != null) classLoader.loadClass(className)
      else Class.forName(className)
      return JavaType.of(clazz).withGenericTypes(genericTypes)
    } catch (e: ClassNotFoundException) {
      throw MarcelSemanticException(token, "Class $className was not found")
    }
  }

  fun clear() {
    _definedTypes.clear()
  }

  fun findMethodByParametersOrThrow(javaType: JavaType, name: String,
                                    positionalArgumentTypes: List<JavaTyped>,
                                    namedParameters: Collection<MethodParameter>, token: LexToken? = null): JavaMethod {
    return findMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, false, token)
      ?: throw MarcelSemanticException(token, "Method $javaType.$name with parameters $namedParameters is not defined")
  }
  fun findMethodByParameters(javaType: JavaType, name: String,
                             positionalArgumentTypes: List<JavaTyped>,
                             namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false, token: LexToken? = null): JavaMethod? {
    val m = doFindMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, excludeInterfaces, token) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, token: LexToken? = LexToken.DUMMY): JavaMethod {
    return findMethod(javaType, name, argumentTypes, false, token) ?: throw MarcelSemanticException(token, "Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, excludeInterfaces: Boolean = false, token: LexToken? = null): JavaMethod? {
    val m = doFindMethod(javaType, name, argumentTypes, excludeInterfaces, token) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else marcelMethods[javaType.className] ?: emptyList()
  }

  fun getClassField(javaType: JavaType, fieldName: String, token: LexToken? = null): JavaClassField {
    return fieldResolver.getField(javaType, fieldName)?.classField ?: throw MarcelSemanticException(token, "Class field $javaType.$fieldName is not defined")
  }

  open fun getDeclaredFields(javaType: JavaType): Collection<CompositeField> {
    return fieldResolver.getAllFields(javaType).values
  }

  fun getInterfaceLambdaMethodOrThrow(type: JavaType, token: LexToken): JavaMethod {
    return getInterfaceLambdaMethod(type) ?: throw MarcelSemanticException(token, "Interface isn't a functional interface")
  }
  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod? {
    return getDeclaredMethods(type).firstOrNull { it.isAbstract && it.name != "equals" && it.name != "hashCode" }
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
                                        excludeInterfaces: Boolean, token: LexToken? = null): JavaMethod? {
    return findMethod(javaType, name, { matchesUnorderedParameters(it, name, positionalArgumentTypes, namedParameters) },
      {candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      }, excludeInterfaces, token)
  }

  private fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<JavaTyped>, excludeInterfaces: Boolean, token: LexToken? = null): JavaMethod? {
    var m = findMethod(javaType, name, { matches(it, name, argumentTypes) },
      {candidates ->  pickMethodCandidate(candidates, name, argumentTypes) }, excludeInterfaces, token)
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList(), false, token)
    }
    return m
  }

  private fun matchesUnorderedParameters(method: JavaMethod, name: String,
                                           positionalArguments: List<JavaTyped>,
                                           arguments: Collection<MethodParameter>): Boolean {
    if (positionalArguments.isNotEmpty()) {
      if (positionalArguments.size > method.parameters.size || positionalArguments.size + arguments.size > method.parameters.size) return false
      for (i in positionalArguments.indices) {
        if (!method.parameters[i].type.isAssignableFrom(positionalArguments[i].type)) {
          return false
        }
      }
    }
    val methodParameters = method.parameters.subList(positionalArguments.size, method.parameters.size)
    if (method.name != name) return false
    if (arguments.size > methodParameters.size || arguments.any { p -> methodParameters.none { it.name == p.name } }) return false
    for (methodParameter in methodParameters) {
      if (arguments.none { methodParameter.type.isAssignableFrom(it.type) && it.name == methodParameter.name } && methodParameter.defaultValue == null) {
        return false
      }
    }
    return true
  }

  fun matches(method: JavaMethod, name: String, types: List<JavaTyped>, strict: Boolean = false): Boolean {
    return method.name == name && matches(method, types, strict)
  }

  // TODO split strict match and match strict=false in separate methods
  fun matches(method: JavaMethod, argumentTypes: List<JavaTyped>, strict: Boolean = false): Boolean {
    if (strict && argumentTypes.size != method.parameters.size
      || !strict && argumentTypes.size > method.parameters.size) return false
    var i = 0
    while (i < argumentTypes.size) {
      val expectedType = method.parameters[i].type
      val actualType = argumentTypes[i].type
      if (!methodParameterTypeMatches(expectedType, actualType, strict)) return false
      i++
    }

    // if all remaining parameters have default value, this is a valid function call
    while (i < method.parameters.size) {
      if (!method.parameters[i].hasDefaultValue) return false
      i++
    }
    return i == max(method.parameters.size, argumentTypes.size)
  }

  private fun methodParameterTypeMatches(expectedType: JavaType, actualType: JavaType, strict: Boolean): Boolean {
    return if (expectedType.isInterface && actualType.isLambda) {
      return getInterfaceLambdaMethod(expectedType) != null // lambda parameter matches will be done by lambda handler
    } else if (!strict) expectedType.isAssignableFrom(actualType)
    else expectedType.raw() == actualType.raw()
  }

  private fun pickMethodCandidate(candidates: List<JavaMethod>, name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    val exactCandidates = candidates.filter { it.exactMatch(name, argumentTypes) }
    return if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(
      if (exactCandidates.isNotEmpty()) exactCandidates else candidates
    )
  }

  fun findMatchingMethod(methods: List<JavaMethod>, name: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    val candidates = methods.filter { matches(it, argumentTypes) }
    return pickMethodCandidate(candidates, name, argumentTypes)
  }

  private fun findMethod(javaType: JavaType, name: String,
                         matcherPredicate: (JavaMethod) -> Boolean,
                         candidatesPicker: (List<JavaMethod>) -> JavaMethod?,
                         excludeInterfaces: Boolean, token: LexToken? = null): JavaMethod? {
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
        val candidate = findMethod(elementsType.arrayType, name, matcherPredicate, candidatesPicker, true, token)
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

  private fun getMoreSpecificMethod(candidates: List<JavaMethod>): JavaMethod? {
    val nonExtensionMethods = candidates.filter { !it.isExtension }
    val toIterate = nonExtensionMethods.ifEmpty { candidates }

    // inspired from Class.searchMethods()
    var m: JavaMethod? = null
    for (candidate in toIterate) {
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

  fun findFieldOrThrow(javaType: JavaType, name: String, token: LexToken? = null): CompositeField {
    return findField(javaType, name) ?: throw MarcelSemanticException(token, "Field $javaType.$name is not defined")
  }

  open fun findField(javaType: JavaType, name: String): CompositeField? {
    if (javaType.isArray && (name == "size" || name == "length")) {
      return CompositeField(MarcelArrayLengthField(javaType, name))
    }
    val field = fieldResolver.getField(javaType, name)
    if (field == null && javaType.implements(JavaType.DynamicObject)) {
      return CompositeField(
        DynamicMethodField(javaType, name, JavaType.DynamicObject,
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("getProperty", String::class.java)),
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("setProperty", String::class.java, JavaType.DynamicObject.realClazz)))
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
            field.addGetter(MethodField(method.returnType, method.propertyName, method.ownerClass, method, null, method is ExtensionJavaMethod))
          } else if (method.isSetter) {
            val field = fieldsMap.computeIfAbsent(method.propertyName) { CompositeField(method.propertyName) }
            field.addSetter(MethodField(method.parameters.first().type, method.propertyName, method.ownerClass, null, method, method is ExtensionJavaMethod))
          }
        }
        return@computeIfAbsent fieldsMap
      }
    }

    private fun loadAllFields(javaType: JavaType): Set<CompositeField> {
      return if (javaType.isLoaded) (javaType.realClazz.fields + javaType.realClazz.declaredFields).map { CompositeField(
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