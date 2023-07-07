package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.getElementsType
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getValuesType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.DynamicMethodField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.scope.ReflectMarcelField
import com.tambapps.marcel.parser.type.*
import marcel.lang.DynamicObject
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods
import org.objectweb.asm.Opcodes

open class JavaTypeResolver constructor(classLoader: MarcelClassLoader?) : AstNodeTypeResolver(classLoader) {

  constructor(): this(null)

  private val classMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  private val classFields = mutableMapOf<String, MutableList<MarcelField>>()

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
      throw MarcelSemanticException((method as? MethodNode)?.token, "Method with $method is already defined")
    }
    methods.add(method)
  }

  override fun defineField(javaType: JavaType, field: MarcelField) {
    if (javaType.isLoaded) {
      throw MarcelSemanticException((field as? FieldNode)?.token, "Cannot define field on loaded class")
    }
    val fields = getMarcelFields(javaType)
    fields.add(field)
  }

  override fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else classMethods[javaType.className] ?: emptyList()
  }

  override fun getClassField(javaType: JavaType, fieldName: String, node: AstNode?): ClassField {
    if (javaType.isLoaded) {
      return try {
        ReflectMarcelField(javaType.realClazz.getDeclaredField(fieldName))
      } catch (e: NoSuchFieldException) {
        ReflectMarcelField(javaType.realClazz.getField(fieldName))
      } catch (e1: NoSuchFieldException) {
        super.getClassField(javaType, fieldName, node)
      }
    } else {
      val field = classFields[javaType.className]?.find { it.name == fieldName }
      return if (field is ClassField) field else super.getClassField(javaType, fieldName, node)
    }
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
                                        excludeInterfaces: Boolean, node: AstNode?): JavaMethod? {
    return findMethod(javaType, name, { it.matchesUnorderedParameters(this, name, positionalArgumentTypes, namedParameters) },
      {candidates ->
        val exactCandidates = candidates.filter { it.parameters.size == namedParameters.size }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      }, excludeInterfaces, node)
  }

  override fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean, node: AstNode?): JavaMethod? {
    var m = findMethod(javaType, name, { it.matches(this, name, argumentTypes) },
      {candidates ->
        val exactCandidates = candidates.filter { it.exactMatch(name, argumentTypes) }
        if (exactCandidates.size == 1) exactCandidates.first() else getMoreSpecificMethod(candidates)
      }, excludeInterfaces, node)
    if (m == null && argumentTypes.isEmpty()) {
      m = findMethodByParameters(javaType, name, argumentTypes, emptyList(), false, node)
    }
    return m
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

  private fun getMarcelFields(javaType: JavaType): MutableList<MarcelField> {
    return classFields.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  private fun getMarcelMethods(javaType: JavaType): MutableList<JavaMethod> {
    // return methods defined from MDK or from marcel source we're currently compiling
    return classMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  override fun findField(javaType: JavaType, name: String, declared: Boolean, node: AstNode?): MarcelField? {
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
        val f = findField(type, name, declared, node)
        if (f != null) return f
        if (type.isLoaded) break // in loaded classes, we already handle super types so no need to go further
        type = type.superType
      }
    }

    // try to find a method field
    val methodFieldName = name.replaceFirstChar { it.uppercase() }
    val getterMethod  = findMethod(javaType, "get$methodFieldName", emptyList(), false, node)
    val setterCandidates = getMarcelMethods(javaType).filter { it.name == "set$methodFieldName" && it.parameters.size  == 1}
    val setterMethod =
      if (setterCandidates.isEmpty()) findMethod(javaType, "set$methodFieldName", listOf(getterMethod?.returnType ?: JavaType.Object), false, node)
      else if (setterCandidates.size == 1) setterCandidates.first()
      else getMoreSpecificMethod(setterCandidates)
    if (getterMethod != null || setterMethod != null) {
      return MethodField.from(javaType, name, getterMethod, setterMethod)
    }

    if (javaType.implements(JavaType.DynamicObject)) {
      return DynamicMethodField(javaType, name, JavaType.DynamicObject,
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("getProperty", String::class.java)),
        ReflectJavaMethod(DynamicObject::class.java.getDeclaredMethod("setProperty", String::class.java, JavaType.DynamicObject.realClazz)),
        Opcodes.ACC_PUBLIC)
    }
    return null
  }

  // ast node type resolver methods
  override fun visit(node: GetFieldAccessOperator): JavaType {
    val field = findFieldOrThrow(node.leftOperand.accept(this), node.rightOperand.name, true, node)
    if (node.directFieldAccess && field !is ClassField) {
      throw MarcelSemanticException(node.token, "Class field ${node.scope.classType}.${node.rightOperand.name} is not defined")
    }
    return if (node.nullSafe) field.type.objectType
    else field.type
  }

  override fun visit(node: GetIndexFieldAccessOperator): JavaType {
    val field = findFieldOrThrow(node.leftOperand.accept(this), node.rightOperand.name, true, node)
    if (node.directFieldAccess && field !is ClassField) {
      throw MarcelSemanticException(node.token, "Class field ${node.scope.classType}.${node.rightOperand.name} is not defined")
    }

    return if (field.type.isArray) field.type.asArrayType.elementsType
    else findMethodOrThrow(field.type, "getAt", node.rightOperand.indexArguments.map { it.accept(this) }, node).actualReturnType
  }

  override fun visit(node: LiteralArrayNode): JavaArrayType {
    if (node.type != null) return node.type!!
    val elementsType = node.getElementsType(this)
      ?: throw MarcelSemanticException(node.token, "Couldn't guess type of array. Use the 'as' keyword to explicitly specify the type")
    return JavaType.arrayType(elementsType)
  }

  override fun visit(node: LiteralMapNode) =
    JavaType.mapType(node.getKeysType(this), node.getValuesType(this))

  override fun visit(node: FunctionCallNode) = node.getMethod(this).actualReturnType
  override fun disposeClass(scriptNode: ClassNode) {
    super.disposeClass(scriptNode)
    classMethods.remove(scriptNode.type.className)
    classFields.remove(scriptNode.type.className)
    scriptNode.innerClasses.forEach { disposeClass(it) }
  }

}