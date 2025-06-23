package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.parser.cst.AbstractMethodCstNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.EnumCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.visitor.ClassCstNodeVisitor
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.imprt.ImportResolverGenerator
import com.tambapps.marcel.semantic.symbol.method.ExtensionMarcelMethod
import com.tambapps.marcel.semantic.symbol.method.JavaConstructorImpl
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.processor.compose.StatementsComposer
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.visitor.AllPathsReturnVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation
import com.tambapps.marcel.semantic.symbol.type.SourceJavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassFieldImpl
import marcel.lang.Binding
import marcel.lang.compile.ExtensionClass
import marcel.util.concurrent.Threadmill
import java.lang.Enum
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.lang.annotation.ElementType
import java.util.concurrent.Callable

// TODO rename SourceFileSemantic
open class MarcelSemantic(
  symbolResolver: MarcelSymbolResolver,
  private val scriptType: JavaType,
  val cst: SourceFileCstNode,
  fileName: String,
) : SemanticCstNodeVisitor(symbolResolver, cst.packageName, fileName), ClassCstNodeVisitor<ClassNode> {

  companion object {
    private const val ENUM_VALUES_FIELD_NAME = "\$VALUES"
  }
  fun resolveImports() {
    imports.add(ImportResolverGenerator.generateImports(symbolResolver, cst.imports, cst.extensionImports))
  }

  fun apply(): ModuleNode {
    try {
      val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)

      loadExtensions()

      try {
        doApply(moduleNode)
      } finally {
        unloadExtensions()
      }
      throwIfHasErrors(moduleNode=moduleNode)
      return moduleNode
    } finally {
      errors.clear()
    }
  }

  private fun doApply(moduleNode: ModuleNode) {
    val scriptCstNode = cst.script

    for (cstClass in cst.classes) {
      val classNode = cstClass.accept(this)
      moduleNode.classes.add(classNode)
      classNode.innerClasses.forEach { innerClassNode ->
        if (innerClassNode is LambdaClassNode) {
          defineLambda(innerClassNode)
        }
      }
    }

    if (scriptCstNode != null) {
      if (scriptCstNode.constructors.isNotEmpty()) {
        error(
          scriptCstNode.constructors.first().token,
          "Cannot define constructors for scripts"
        )
      }
      val scriptNode = scriptCstNode.accept(this)
      moduleNode.classes.add(scriptNode)
    }
    moduleNode.classes.addAll(lambdaMap.values.filter { it.isTopLevel })
  }

  private fun defineClass(classCstNode: ClassCstNode, recursive: Boolean) {
    val superType = classCstNode.superType?.let { resolve(it) } ?: JavaType.Object
    val interfaces = classCstNode.interfaces.map { resolve(it) }
    val classType = symbolResolver.defineType(
      token = classCstNode.token,
      visibility = Visibility.fromTokenType(classCstNode.access.visibility),
      className = classCstNode.className,
      superClass = superType,
      interfaces = interfaces,
      isInterface = false, // not supported yet
      isFunctionalInterface = false,
      isAbstract = false, // not supported
      isAnnotation = false, // not supported
      isScript = classCstNode.isScript,
      isEnum = classCstNode.isEnum,
      isFinal = classCstNode.access.isFinal || classCstNode.isEnum,
      isExtensionType = classCstNode.isExtensionClass,
      extendedType = classCstNode.forExtensionType?.let { resolve(it) }
    )
    defineClassMembers(classCstNode, classType, recursive=recursive)
  }

  fun defineClassMembers(classCstNode: ClassCstNode, classType: JavaType, recursive: Boolean = true) = useScope(
    ClassScope(symbolResolver, classType, classCstNode.forExtensionType?.let { resolve(it) }, imports)
  ) {
    loadExtensions()
    try {
      if (classCstNode.isEnum) {
        classCstNode as EnumCstNode
        for (enumName in classCstNode.names) {
          symbolResolver.defineField(JavaClassFieldImpl(classType, enumName, classType, Nullness.NOT_NULL, isFinal = true, Visibility.PUBLIC, isStatic = true, isSettable = false))
        }
        symbolResolver.defineMethod(classType, MarcelMethodImpl(classType, Visibility.PUBLIC, "valueOf",
          Nullness.NOT_NULL,
          mutableListOf(
          MethodParameter(JavaType.String, Nullness.NOT_NULL, "name")
        ), classType, isStatic = true))
        symbolResolver.defineMethod(classType, MarcelMethodImpl(classType, Visibility.PUBLIC, "values", Nullness.NOT_NULL, emptyList(), classType.arrayType, isStatic = true))

      }
      if (classCstNode.isExtensionClass) {
        val extendedCstType = classCstNode.forExtensionType
        val extendedType = extendedCstType?.let(this::resolve)
        classCstNode.methods.forEach { m ->
          if (extendedType != null && m.parameters.firstOrNull()?.name == ExtensionMarcelMethod.THIS_PARAMETER_NAME) {
            error(m.tokenEnd, "First parameter of a method extension cannot be named ${ExtensionMarcelMethod.THIS_PARAMETER_NAME}")
          }
          val extensionMethod = if (m.accessNode.isStatic) {
            if (extendedType == null) {
              throw MarcelSemanticException(classCstNode, "Cannot define static method in a non-type specific extension class")
            }
            ExtensionMarcelMethod.staticMethodExtension(toJavaMethod(classType, extendedType, m), extendedType)
          } else {
            if (extendedCstType != null) {
              // adding self parameter
              m.parameters.add(
                0,
                MethodParameterCstNode(m, m.tokenStart, m.tokenEnd, ExtensionMarcelMethod.THIS_PARAMETER_NAME, extendedCstType, null, emptyList(), false, false)
              )
            }
            m.accessNode.isStatic = true // extension method is always java-static. that's why we are considering them static from now on
            ExtensionMarcelMethod.instanceMethodExtension(toJavaMethod(classType, extendedType, m))
          }
          // define extension method so that we can reference them in methods of this extension class
          symbolResolver.defineMethod(extensionMethod.marcelOwnerClass, extensionMethod)
        }
      }
      classCstNode.methods.forEach {
        symbolResolver.defineMethod(
          classType,
          toJavaMethod(classType, classCstNode.forExtensionType?.let(this::resolve), it)
        )
      }
      classCstNode.fields.forEach { symbolResolver.defineField(classType, toMarcelField(classType, it)) }
      classCstNode.constructors.forEach { symbolResolver.defineMethod(classType, toJavaConstructor(classType, it)) }

      if (recursive) {
        classCstNode.innerClasses.forEach { defineClass(it, recursive) }
      }
    } finally {
      unloadExtensions()
    }
  }

  /*
   * Class semantic
   */
  private open inner class ClassSemantic<T: ClassCstNode>(
    protected val node: T,
    protected val classNode: ClassNode,
    protected val classScope: ClassScope
  ) {
    protected val classType = classNode.type
    protected val fieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
    protected val staticFieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()

    fun apply() {
      // extension types check
      if (classNode.forExtensionType != null) {
        if (node.constructors.isNotEmpty()) {
          error(node, "Extension classes cannot have constructors")
        }
        node.fields.forEach { f ->
          if (!f.access.isStatic) {
            error(f, "Cannot have non static members in extension class")
          }
        }
        classNode.annotations.add(AnnotationNode(ExtensionClass::class.javaAnnotationType,
          classNode.forExtensionType?.let { forExtensionType ->
            listOf(
              JavaAnnotation.Attribute(name = "forClass", type = JavaType.Clazz, forExtensionType)
            )
          } ?: listOf()
          , classNode.tokenStart, classNode.tokenEnd))
      }

      // must handle inner classes BEFORE handling this class being an inner class because in this case constructors will be modified
      node.innerClasses.forEach {
        classNode.innerClasses.add(it.accept(this@MarcelSemantic))
      }
      processConstructors()

      node.annotations.forEach {
        val annotation = visit(it, ElementType.TYPE)
        classNode.annotations.add(annotation)
        (classNode.type as? SourceJavaType)?.addAnnotation(annotation)
      }

      processMethods()
      processFields()

      if (fieldInitialValueMap.isNotEmpty()) {
        val fieldAssignmentStatements = toFieldAssignmentStatements(classType, fieldInitialValueMap, false)
        for (constructorNode in classNode.constructors) {
          // add at one because the first statement is the super call
          constructorNode.blockStatement.statements.addAll(
            1,
            fieldAssignmentStatements
          )
        }
      }
      if (staticFieldInitialValueMap.isNotEmpty()) {
        val staticInitMethod = getOrCreateStaticInitialisationMethod(classNode)
        staticInitMethod.blockStatement.statements.addAll(
          0,
          toFieldAssignmentStatements(classType, staticFieldInitialValueMap, true)
        )
      }
      onEnd()
    }

    protected open fun onEnd() {}
    protected open fun processConstructors() {
      node.constructors.forEach { classNode.methods.add(constructorNode(classNode, it, classScope)) }
      if (classNode.constructorCount == 0) {
        // default no arg constructor
        val noArgConstructor = noArgConstructor(
          classNode, symbolResolver,
          visibility = if (classNode.isExtensionClass || classNode.isEnum) Visibility.PRIVATE else Visibility.PUBLIC
        )
        classNode.methods.add(noArgConstructor)
        symbolResolver.defineMethod(classType, noArgConstructor)
      }
    }

    protected open fun processMethods() {
      node.methods.forEach { classNode.methods.add(methodNode(classNode, it, classScope)) }
    }

    protected open fun processFields() {
      node.fields.forEach { cstFieldNode ->
        val fieldNode = FieldNode(
          resolve(cstFieldNode.type), cstFieldNode.name, classType,
          Nullness.of(cstFieldNode.isNullable),
          cstFieldNode.annotations.map { visit(it, ElementType.FIELD) },
          cstFieldNode.access.isFinal, Visibility.fromTokenType(cstFieldNode.access.visibility),
          cstFieldNode.access.isStatic, cstFieldNode.tokenStart, cstFieldNode.tokenEnd, identifierToken = cstFieldNode.identifierToken
        )
        if (classNode.fields.any { it.name == fieldNode.name }) {
          error(cstFieldNode, "Field ${cstFieldNode.name} already exists")
        }
        classNode.fields.add(fieldNode)

        if (cstFieldNode.initialValue != null) {
          // need to create method scope to properly resolve everything
          if (fieldNode.isStatic) {
            val stInitMethod = getOrCreateStaticInitialisationMethod(classNode)
            useScope(MethodScope(classScope, stInitMethod)) {
              staticFieldInitialValueMap[fieldNode] = cstFieldNode.initialValue!!.accept(this@MarcelSemantic, fieldNode.type)
            }
          } else {
            fieldInitialValueMap[fieldNode] = useScope(
              MethodScope(
                classScope,
                // static block initialization is considered synthetic
                JavaConstructorImpl(Visibility.PRIVATE, isVarArgs = false, isSynthetic = true, classType, emptyList())
              )
            ) {
              cast(fieldNode.type, cstFieldNode.initialValue!!.accept(this@MarcelSemantic, fieldNode.type))
            }
          }
        }
      }
    }
  }

  private inner class RegularClassSemantic(node: RegularClassCstNode, classNode: ClassNode, classScope: ClassScope) :
    ClassSemantic<RegularClassCstNode>(node, classNode, classScope)

  private inner class ScriptClassSemantic(node: ScriptCstNode, classNode: ClassNode, classScope: ClassScope) :
    ClassSemantic<ScriptCstNode>(node, classNode, classScope) {

    override fun processConstructors() {
      super.processConstructors()
      // need the binding constructor. the no-arg constructor should already have been added
      classNode.methods.add(scriptBindingConstructor(classNode, symbolResolver, scriptType))
    }

    override fun processMethods() {
      // add the run method
      val runMethod = MethodNode(
        name = "run",
        visibility = Visibility.PUBLIC, returnType = JavaType.Object,
        isStatic = false,
        ownerClass = classType,
        parameters = mutableListOf(MethodParameter(JavaType.String.arrayType, Nullness.UNKNOWN, "args")),
        tokenStart = cst.tokenStart,
        tokenEnd = cst.tokenEnd,
        nullness = Nullness.NULLABLE
      )

      fillMethodNode(
        classScope,
        runMethod,
        node.runMethodStatements,
        emptyList(),
        scriptRunMethod = true,
        isAsync = false
      )
      classNode.methods.add(runMethod)
      super.processMethods()
    }

    override fun onEnd() {
      // need this to be at the end of this function
      classNode.innerClasses.forEach { innerClassNode ->
        if (innerClassNode is LambdaClassNode) {
          defineLambda(innerClassNode)
        }
      }
    }
  }


  // note to self https://chatgpt.com/c/6759239f-ede4-8012-b99b-daa74614b684
  private inner class EnumClassSemantic(node: EnumCstNode, classNode: ClassNode, classScope: ClassScope) :
    ClassSemantic<EnumCstNode>(node, classNode, classScope) {

    private val valuesField = FieldNode(
      type = classType.arrayType,
      name = ENUM_VALUES_FIELD_NAME,
      owner = classType, annotations = emptyList(),
      isFinal = true,
      visibility = Visibility.PRIVATE,
      isStatic = true,
      isSynthetic = true,
      nullness = Nullness.NOT_NULL,
      tokenStart = classNode.tokenStart,
      tokenEnd = classNode.tokenEnd)

    override fun processConstructors() {
      // enum classes actually have one private constructor that is called to instantiate every instance
      val constructorNode = compose(
        MethodNode(
          name = MarcelMethod.CONSTRUCTOR_NAME,
          parameters = mutableListOf(
            MethodParameter(JavaType.String, Nullness.NOT_NULL, "name", isSynthetic = true),
            MethodParameter(JavaType.int, Nullness.NOT_NULL, "ordinal", isSynthetic = true)
          ),
          visibility = Visibility.PRIVATE,
          returnType = JavaType.void,
          isStatic = false,
          isVarArgs = false,
          ownerClass = classType,
          tokenStart = node.tokenStart,
          tokenEnd = node.tokenEnd,
          nullness = Nullness.NOT_NULL)
      ) { scope ->
        stmt(superConstructorCall(
          method = symbolResolver.findConstructor(java.lang.Enum::class.javaType, listOf(JavaType.String, JavaType.int))!!,
          listOf(ref(scope.getMethodParameterVariable(0)), ref(scope.getMethodParameterVariable(1)))))
        returnVoidStmt()
      }

      classNode.methods.add(constructorNode)
      symbolResolver.defineMethod(classType, constructorNode)
    }

    override fun processFields() {
      val enumConstructor = classNode.constructors.first() // doesn't support declaring constructors for enums so we can just assume there is only one, the default one

      // enum fields
      val valueFields = node.names.mapIndexed { index, name ->
        val fieldNode = FieldNode(
          type = classType,
          name = name,
          nullness = Nullness.NOT_NULL,
          owner = classType, annotations = emptyList(),
          isFinal = true,
          isStatic = true,
          isEnum = true,
          visibility = Visibility.PUBLIC,
          tokenStart = classNode.tokenStart, tokenEnd = classNode.tokenEnd)
        classNode.fields.add(fieldNode)
        staticFieldInitialValueMap[fieldNode] = NewInstanceNode(classType, enumConstructor, listOf(StringConstantNode(name, node), IntConstantNode(node.token, index)), node.token)
        fieldNode
      }

      // values field, for values() method that will clone this array
      classNode.fields.add(valuesField)

      staticFieldInitialValueMap[valuesField] = ArrayNode(
        elements = valueFields.asSequence().map { ReferenceNode(variable = it, token = node.tokenEnd) }.toMutableList(),
        node = node,
        type = classType.arrayType
      )
    }

    override fun processMethods() {
      val valuesMethod = compose(MethodNode(
        name = "values",
        parameters = mutableListOf(),
        visibility = Visibility.PUBLIC,
        returnType = valuesField.type,
        isStatic = true,
        isVarArgs = false,
        ownerClass = classType,
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd,
        nullness = Nullness.NOT_NULL)
      ) {
        returnStmt(fCall(name = "clone", arguments = emptyList(), owner = ref(valuesField)))
      }

      val valueOfMethod = compose(MethodNode(
        name = "valueOf",
        parameters = mutableListOf(
          MethodParameter(JavaType.String, Nullness.NOT_NULL, "name"),
        ),
        visibility = Visibility.PUBLIC,
        returnType = classNode.type,
        isStatic = true,
        isVarArgs = false,
        ownerClass = classType,
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd,
        nullness = Nullness.NOT_NULL)
      ) { scope ->
        val nameParameterRef = ref(scope.getMethodParameterVariable(0))
        ifStmt(isEqualExpr(nameParameterRef, NullValueNode(node.token))) {
          throwStmt(constructorCall(
            method = symbolResolver.findConstructor(NullPointerException::class.javaType, listOf(JavaType.String))!!,
            listOf(string("Name is null")
            )
          ))
        }
        forInArrayStmt(array = ref(valuesField)) { _, forVar ->
          ifStmt(isEqualExpr(
            fCall(name = "name", arguments = emptyList(), owner = ref(forVar)),
            nameParameterRef
          )) {
            returnStmt(ref(forVar))
          }
        }
        throwStmt(constructorCall(
          method = symbolResolver.findConstructor(IllegalArgumentException::class.javaType, listOf(JavaType.String))!!,
          listOf(string(listOf(string("No enum constant ${classNode.type}."), nameParameterRef))
          )
        ))
      }

      classNode.methods.apply {
        add(valuesMethod)
        add(valueOfMethod)
      }
      super.processMethods()
    }
  }

  override fun visit(node: RegularClassCstNode) = classNode(node, ::RegularClassSemantic)

  override fun visit(node: EnumCstNode) = classNode(node, ::EnumClassSemantic)

  override fun visit(node: ScriptCstNode) = classNode(node, ::ScriptClassSemantic)

  private inline fun <T: ClassCstNode> classNode(node: T, classNodeCreatorCreator: (T, ClassNode, ClassScope) -> ClassSemantic<T>): ClassNode =
    useScope(ClassScope(symbolResolver, symbolResolver.of(node.className, emptyList(), node.token), node.forExtensionType?.let(this::resolve), imports)) { classScope ->
      val classType = classScope.classType
      val classNode = ClassNode(
        classType, Visibility.fromTokenType(node.access.visibility),
        classScope.forExtensionType,
        isStatic = classType.outerTypeName != null && node.access.isStatic,
        isScript = node.isScript,
        isEnum = node.isEnum,
        isFinal = node.access.isFinal || node.isEnum,
        fileName = fileName,
        tokenStart = cst.tokenStart, tokenEnd = cst.tokenEnd
      )
      classNodeMap[classType] = classNode
      classNodeCreatorCreator.invoke(node, classNode, classScope).apply()
      return@useScope classNode
    }

  private fun getOrCreateStaticInitialisationMethod(classNode: ClassNode): MethodNode {
    val m = classNode.methods.find { it.name == MarcelMethod.STATIC_INITIALIZATION_BLOCK }
    if (m != null) return m
    val newMethod = staticInitialisationMethod(classNode)
    classNode.methods.add(newMethod)
    return newMethod
  }

  private fun scriptBindingConstructor(
    classNode: ClassNode,
    symbolResolver: MarcelSymbolResolver,
    scriptType: JavaType
  ): MethodNode {
    val parameter = MethodParameter(Binding::class.javaType, Nullness.NOT_NULL, "binding")
    return compose(
      MethodNode(
        MarcelMethod.CONSTRUCTOR_NAME,
        Nullness.NOT_NULL,
        mutableListOf(parameter),
        Visibility.PUBLIC,
        JavaType.void,
        false,
        classNode.tokenStart,
        classNode.tokenEnd,
        JavaType.void,
      )
    ) {
      stmt(superConstructorCall(
        symbolResolver.findConstructor(scriptType, listOf(parameter))!!,
        listOf(ref(parameter))
      ))
      returnVoidStmt()
    }
  }

  private fun toFieldAssignmentStatements(
    classType: JavaType,
    map: Map<FieldNode, ExpressionNode>,
    isStaticContext: Boolean
  ): List<ExpressionStatementNode> {
    return map.map { entry ->
      val field = entry.key
      val initialValue = entry.value
      ExpressionStatementNode(
        VariableAssignmentNode(
          variable = field,
          owner = if (isStaticContext) null else ThisReferenceNode(classType, field.token),
          expression = cast(field.type, initialValue),
          tokenStart = field.tokenStart, tokenEnd = initialValue.tokenEnd
        )
      )
    }
  }

  /*
   * Method nodes
   */
  private fun methodNode(classNode: ClassNode, methodCst: MethodCstNode, classScope: ClassScope): MethodNode {
    val (returnType, asyncReturnType) = resolveReturnType(methodCst)
    val methodNode = toMethodNode(
      classNode, methodCst, methodCst.name,
      returnType, asyncReturnType, classScope.classType, Nullness.of(methodCst.isReturnTypeNullable)
    )
    val superMethod = symbolResolver.findSuperMethod(methodNode)
    if (superMethod != null && methodCst.isOverride == false) {
      error(methodCst, "override keyword should be used for overridden methods")
    } else if (superMethod == null && methodCst.isOverride == true) {
      error(methodCst, "method $methodNode doesn't override anything")
    }
    fillMethodNode(
      classScope, methodNode, methodCst.statements, methodCst.annotations,
      isAsync = methodCst.isAsync,
      isSingleStatementMethod = methodCst.isSingleStatementFunction
    )
    return methodNode
  }

  private fun constructorNode(
    classNode: ClassNode,
    constructorCstNode: ConstructorCstNode,
    classScope: ClassScope
  ): MethodNode {
    val constructorNode = toConstructorNode(classNode, constructorCstNode, classScope.classType)

    val outerClassFields = generateOutClassFields(classNode.type, classNode)
    if (outerClassFields.isNotEmpty()) {
      // undefining method as the signature will change (we erase the definition of the 'definition' phase. THIS IS IMPORTANT. DON'T REMOVE ME
      symbolResolver.undefineMethod(classNode.type, constructorNode)
      for (i in outerClassFields.indices) {
        val outerClassField = outerClassFields[i]
        // adding parameter at the beginning
        constructorNode.parameters.add(i, MethodParameter(outerClassField.type, outerClassField.nullness, outerClassField.name))
      }
      // now that the method has been updated we're redefining it. THIS IS IMPORTANT. DON'T REMOVE ME
      symbolResolver.defineMethod(classNode.type, constructorNode)
    }

    fillMethodNode(
      classScope,
      constructorNode,
      constructorCstNode.statements,
      constructorCstNode.annotations,
      isAsync = false
    )

    val firstStatement = constructorNode.blockStatement.statements.firstOrNull()
    if (firstStatement == null || firstStatement !is ExpressionStatementNode
      || firstStatement.expressionNode !is ThisConstructorCallNode && firstStatement.expressionNode !is SuperConstructorCallNode
    ) {
      val superType = classScope.classType.superType!!
      val superConstructorMethod = symbolResolver.findConstructor(superType, emptyList())
        ?: throw MarcelSemanticException(constructorNode.token, "Class $superType doesn't have a no-arg constructor")
      constructorNode.blockStatement.statements.add(
        0,
        ExpressionStatementNode(
          SuperConstructorCallNode(
            superType,
            superConstructorMethod,
            emptyList(),
            constructorNode.tokenStart,
            constructorNode.tokenEnd
          )
        )
      )
    }

    // because we want to add thisParameter varAssign AFTER outer class fields assignments
    val thisParameterInstructionsStart = 1 + outerClassFields.size
    if (outerClassFields.isNotEmpty()) {
      // now we also need to assign these outer class references fields
      for (i in outerClassFields.indices) {
        val outerClassField = outerClassFields[i]
        constructorNode.blockStatement.statements.add(
          i + 1, // +1 because first statement should be super call
          ExpressionStatementNode(
            VariableAssignmentNode(
              owner = ThisReferenceNode(classNode.type, classNode.token),
              variable = outerClassField,
              expression = ReferenceNode(
                variable = LocalVariable(
                  outerClassField.type, outerClassField.name, nbSlots = outerClassField.type.nbSlots,
                  // we can out i+1 here because we know these inner outer class arguments are references, so always take one slot
                  index = i + 1, isFinal = false, nullness = outerClassField.nullness
                ), token = classNode.token
              ),
              node = constructorCstNode
            )
          )
        )
      }
    }

    /*
     * Handling this parameters
     */
    // going in reverse to add in order assignments in correct order
    for (methodCstParameter in constructorCstNode.parameters) {
      if (!methodCstParameter.thisParameter) continue
      val param = constructorNode.parameters.find { it.name == methodCstParameter.name }!!
      val field = symbolResolver.getClassField(classScope.classType, param.name, constructorNode.token)
      constructorNode.blockStatement.statements.add(
        thisParameterInstructionsStart, // 1 because 0 is the super call
        ExpressionStatementNode(
          VariableAssignmentNode(
            owner = ThisReferenceNode(classScope.classType, constructorCstNode.token), variable = field,
            // using index of method parameter. +1 because not in static context
            expression = ReferenceNode(
              variable = constructorNode.toLocalVariable(param),
              token = constructorCstNode.token
            ), node = constructorCstNode
          )
        )
      )
    }
    return constructorNode
  }

  private fun toConstructorNode(classNode: ClassNode, methodCst: AbstractMethodCstNode, classType: JavaType) =
    toMethodNode(classNode, methodCst, MarcelMethod.CONSTRUCTOR_NAME, JavaType.void, asyncReturnType = null, classType, Nullness.NOT_NULL)

  private fun toMethodNode(
    classNode: ClassNode, methodCst: AbstractMethodCstNode, methodName: String,
    returnType: JavaType, asyncReturnType: JavaType?, classType: JavaType, nullness: Nullness
  ): MethodNode {
    val visibility = Visibility.fromTokenType(methodCst.accessNode.visibility)
    val isStatic = methodCst.accessNode.isStatic
    val parameters = mutableListOf<MethodParameter>()
    methodCst.parameters.forEachIndexed { index, methodParameterCstNode ->
      parameters.add(toMethodParameterNode(classNode, visibility, isStatic, index, methodName, methodParameterCstNode))
    }
    return MethodNode(
      name = methodName,
      nullness = nullness,
      visibility = visibility,
      returnType = returnType,
      isStatic = isStatic,
      tokenStart = methodCst.tokenStart,
      tokenEnd = methodCst.tokenEnd,
      parameters = parameters,
      ownerClass = classType,
      asyncReturnType = asyncReturnType,
      isVarArgs = methodCst.isVarArgs,
      identifierToken = methodCst.identifierToken
    )
  }

  private fun fillMethodNode(
    classScope: ClassScope, methodeNode: MethodNode,
    cstStatements: List<StatementCstNode>,
    annotations: List<AnnotationCstNode>,
    isAsync: Boolean,
    isSingleStatementMethod: Boolean = false,
    scriptRunMethod: Boolean = false
  ): Unit = useScope(MethodScope(classScope, methodeNode)) {

    // filling annotations
    annotations.forEach { methodeNode.annotations.add(visit(it, ElementType.METHOD)) }
    if (isAsync) {
      addAsyncAnnotation(methodeNode)
    }

    val statements = if (isSingleStatementMethod && cstStatements.size == 1
      && methodeNode.returnType != JavaType.void
      && methodeNode.asyncReturnType != JavaType.void // asyncType == null || asyncType != void
      && cstStatements.first() is ExpressionStatementCstNode
    ) {
      val statement = cstStatements.first() as ExpressionStatementCstNode
      mutableListOf(
        ReturnCstNode(statement, statement.expressionNode, statement.tokenStart, statement.tokenEnd).accept(
          this
        )
      )
    } else if (scriptRunMethod && cstStatements.isNotEmpty()) {
      // if it's the script run method and the last statement is an expression statement, It becomes the return value of the run() method
      blockStatements(cstStatements.subList(0, cstStatements.size - 1)).apply {
        var lastStatement = cstStatements.last().accept(this@MarcelSemantic)
        if (lastStatement is ExpressionStatementNode && lastStatement.expressionNode.type != JavaType.void) {
          lastStatement = ReturnStatementNode(cast(JavaType.Object, lastStatement.expressionNode))
        }
        add(lastStatement)
      }
    } else blockStatements(cstStatements)

    if (!AllPathsReturnVisitor.test(statements)) {
      if (methodeNode.returnType == JavaType.void || methodeNode.asyncReturnType == JavaType.void) {
        statements.add(returnVoid(methodeNode))
      } else if (scriptRunMethod) {
        statements.add(ReturnStatementNode(NullValueNode(methodeNode.token)))
      } else {
        error(
          methodeNode,
          "Not all paths return a value in method ${methodeNode.ownerClass}.${methodeNode.name}()"
        )
      }
    }
    if (!methodeNode.isAsync) {
      methodeNode.blockStatement.addAll(statements)
    } else {
      // generate doMethod which will have the actual statements
      val doMethodNode = MethodNode(
        name = "_doAsync" + methodeNode.name[0].uppercase() + methodeNode.name.substring(1),
        parameters = methodeNode.parameters,
        visibility = Visibility.INTERNAL,
        returnType = methodeNode.asyncReturnType!!,
        isStatic = methodeNode.isStatic,
        tokenStart = methodeNode.tokenStart,
        tokenEnd = methodeNode.tokenEnd,
        ownerClass = methodeNode.ownerClass,
        nullness = Nullness.UNKNOWN
      )
      doMethodNode.blockStatement.addAll(statements)
      val classNode = currentClassNode ?: throw MarcelSemanticException(
        methodeNode.token,
        "Cannot create async method in such context"
      )
      classNode.methods.add(doMethodNode)
      val returnsVoid = methodeNode.asyncReturnType == JavaType.void
      val interfaceType = if (returnsVoid) Runnable::class.javaType else Callable::class.javaType

      val (lambdaClassNode, lambdaMethod, newInstanceNode) = createLambdaNode(
        outerClassNode = classNode,
        references = methodeNode.parameters.map {
          ReferenceNode(
            variable = currentMethodScope.findLocalVariable(it.name)!!,
            token = methodeNode.token
          )
        },
        lambdaMethodParameters = emptyList(),
        // as generic types aren't supported in Marcel, we need to return an Object because otherwise Java wouldn't recognize implemented method
        returnType = if (doMethodNode.returnType == JavaType.void) JavaType.void else JavaType.Object,
        interfaceType = interfaceType,
        tokenStart = methodeNode.tokenStart,
        tokenEnd = methodeNode.tokenEnd
      )

      compose(lambdaMethod) { scope ->
        val fCall = fCall(
          arguments = doMethodNode.parameters.map {
            ReferenceNode(
              owner = ThisReferenceNode(
                lambdaClassNode.type,
                lambdaClassNode.token
              ), variable = scope.findField(it.name)!!, token = methodeNode.tokenStart
            )
          },
          owner = if (doMethodNode.isStatic) null else getOuterLevelReference(
            scope,
            methodeNode.tokenStart,
            ClassOuterLevels.OUTER
          ),
          method = doMethodNode,
          tokenStart = methodeNode.tokenStart,
          tokenEnd = methodeNode.tokenEnd
        )
        if (returnsVoid) {
          stmt(fCall)
          returnStmt(VoidExpressionNode(lambdaMethod))
        } else {
          returnStmt(fCall)
        }
      }

      // now fill the method with the call to Threadmill
      methodeNode.blockStatement.add(
        ReturnStatementNode(
          fCall(
            ownerType = Threadmill::class.javaType,
            name = if (returnsVoid) "runAsync" else "supplyAsync",
            arguments = listOf(newInstanceNode),
            tokenStart = methodeNode.tokenStart,
            tokenEnd = methodeNode.tokenEnd
          )
        )
      )
    }
  }

  private fun loadExtensions() {
    imports.extensionTypes.forEach(symbolResolver::loadExtension)
  }

  private fun unloadExtensions() {
    imports.extensionTypes.forEach(symbolResolver::unloadExtension)
  }
}