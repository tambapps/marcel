package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.AbstractMethodCstNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.EnumCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
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
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.imprt.ImportResolverGenerator
import com.tambapps.marcel.semantic.method.ExtensionJavaMethod
import com.tambapps.marcel.semantic.method.JavaConstructorImpl
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.SourceJavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor
import marcel.util.concurrent.Threadmill
import java.lang.annotation.ElementType
import java.util.concurrent.Callable

open class MarcelSemantic(
  symbolResolver: MarcelSymbolResolver,
  private val scriptType: JavaType,
  val cst: SourceFileCstNode,
  val fileName: String,
) : SemanticCstNodeVisitor(symbolResolver, cst.packageName) {

  fun resolveImports() {
    imports.add(ImportResolverGenerator.generateImports(symbolResolver, cst.imports))
  }

  fun apply(): ModuleNode {
    val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)

    // load extension types
    val extensionTypes = cst.extensionImports.map(this::resolve)
    extensionTypes.forEach(symbolResolver::loadExtension)

    try {
      doApply(moduleNode)
    } finally {
      extensionTypes.forEach(symbolResolver::unloadExtension)
    }

    return moduleNode
  }

  private fun doApply(moduleNode: ModuleNode) {
    val scriptCstNode = cst.script

    for (cstClass in cst.classes) {
      val classNode = classNode(symbolResolver.of(cstClass.className, emptyList(), cstClass.token), cstClass)
      moduleNode.classes.add(classNode)
      classNode.innerClasses.forEach { innerClassNode ->
        if (innerClassNode is LambdaClassNode) {
          defineLambda(innerClassNode)
        }
      }
    }

    if (scriptCstNode != null) {
      if (scriptCstNode.constructors.isNotEmpty()) {
        throw MarcelSemanticException(
          scriptCstNode.constructors.first().token,
          "Cannot define constructors for scripts"
        )
      }
      val classType = symbolResolver.of(scriptCstNode.className)
      val scriptNode = classNode(classType, scriptCstNode)
      moduleNode.classes.add(scriptNode)
    }
  }

  private fun defineClass(classCstNode: ClassCstNode) {
    val superType = classCstNode.superType?.let { resolve(it) } ?: JavaType.Object
    val interfaces = classCstNode.interfaces.map { resolve(it) }
    val classType = symbolResolver.defineType(
      classCstNode.tokenStart, Visibility.fromTokenType(classCstNode.access.visibility),
      classCstNode.className, superType,
      // don't support interfaces for now
      isInterface = false, interfaces,
      isScript = classCstNode.isScript,
      isEnum = classCstNode.isEnum
    )
    defineClassMembers(classCstNode, classType)
  }

  fun defineClassMembers(classCstNode: ClassCstNode, classType: JavaType, recursive: Boolean = true) = useScope(
    ClassScope(symbolResolver, classType, classCstNode.forExtensionType?.let { resolve(it) }, imports)
  ) {
    if (classCstNode.isExtensionClass) {
      val extensionCstType = classCstNode.forExtensionType!!
      val extensionType = resolve(extensionCstType)
      classCstNode.methods.forEach { m ->
        m.accessNode.isStatic = true
        // extension class methods first parameter is self, which can be considered as this
        m.parameters.add(
          0,
          MethodParameterCstNode(m, m.tokenStart, m.tokenEnd, "self", extensionCstType, null, emptyList(), false)
        )
        // define extension method so that we can reference them in methods of this extension class
        symbolResolver.defineMethod(
          extensionType,
          ExtensionJavaMethod(toJavaMethod(classType, classCstNode.forExtensionType?.let(this::resolve), m))
        )
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
      classCstNode.innerClasses.forEach { defineClass(it) }
    }
  }

  // TODO try to use polymorphism to avoid having to if/else everywhere about isEnum, isScript
  private fun classNode(classType: JavaType, node: ClassCstNode): ClassNode =
    useScope(ClassScope(symbolResolver, classType, node.forExtensionType?.let(this::resolve), imports)) { classScope ->
      val classNode = ClassNode(
        classType, Visibility.fromTokenType(node.access.visibility),
        classScope.forExtensionType,
        isStatic = classType.outerTypeName != null && node.access.isStatic,
        isScript = node.isScript,
        isEnum = node.isEnum,
        fileName = fileName,
        cst.tokenStart, cst.tokenEnd
      )
      classNodeMap[classType] = classNode

      // extension types check
      if (classNode.forExtensionType != null) {
        if (node.constructors.isNotEmpty()) {
          throw MarcelSemanticException(node, "Extension classes cannot have constructors")
        }
        node.fields.forEach { f ->
          if (!f.access.isStatic) {
            throw MarcelSemanticException(f, "Cannot have non static members in extension class")
          }
        }
      }

      // must handle inner classes BEFORE handling this class being an inner class because in this case constructors will be modified
      node.innerClasses.forEach {
        classNode.innerClasses.add(classNode(symbolResolver.of(it.className, emptyList(), it.token), it))
      }
      node.constructors.forEach { classNode.methods.add(constructorNode(classNode, it, classScope)) }
      if (classNode.constructorCount == 0) {
        // default no arg constructor
        val noArgConstructor = SemanticHelper.noArgConstructor(
          classNode, symbolResolver,
          visibility = if (classNode.isExtensionClass || classNode.isEnum) Visibility.PRIVATE else Visibility.PUBLIC
        )
        classNode.methods.add(noArgConstructor)
        symbolResolver.defineMethod(classType, noArgConstructor)
      }

      node.annotations.forEach {
        val annotation = visit(it, ElementType.TYPE)
        classNode.annotations.add(annotation)
        (classNode.type as? SourceJavaType)?.addAnnotation(annotation)
      }

      // will be used later but needs to be declared here because of enums
      val staticFieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
      if (node is ScriptCstNode) {
        // need the binding constructor. the no-arg constructor should have been added in the classNode() method
        classNode.methods.add(SemanticHelper.scriptBindingConstructor(classNode, symbolResolver, scriptType))
        // add the run method
        val runMethod = SemanticHelper.scriptRunMethod(classType, cst)
        fillMethodNode(
          classScope,
          runMethod,
          node.runMethodStatements,
          emptyList(),
          scriptRunMethod = true,
          isAsync = false
        )
        classNode.methods.add(runMethod)
      } else if (node is EnumCstNode) {
        // create static fields
        node.names.forEachIndexed { index, name ->
          val enumConstructor = classNode.constructors.first() // doesn't support declaring constructors for enums so we can just assume there is only one, the default one
          val fieldNode = FieldNode(classType, name, classType, emptyList(), isFinal = true, Visibility.PUBLIC, isStatic = true, classNode.tokenStart, classNode.tokenEnd)
          classNode.fields.add(fieldNode)
          staticFieldInitialValueMap[fieldNode] = NewInstanceNode(classType, enumConstructor, listOf(StringConstantNode(name, node), IntConstantNode(node.token, index)), node.token)
        }

        // TODO add values() method and valueOf
        // TODO default constructor of enum should not be a noArg. It should have a name and ordinal argument and should call super(String name, int ordinal)
        TODO("Doesn't handle enums yet")
      }
      // iterating with i because we might add methods while
      node.methods.forEach { classNode.methods.add(methodNode(classNode, it, classScope)) }
      val fieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
      node.fields.forEach { cstFieldNode ->
        val fieldNode = FieldNode(
          resolve(cstFieldNode.type), cstFieldNode.name, classType,
          cstFieldNode.annotations.map { visit(it, ElementType.FIELD) },
          cstFieldNode.access.isFinal, Visibility.fromTokenType(cstFieldNode.access.visibility),
          cstFieldNode.access.isStatic, cstFieldNode.tokenStart, cstFieldNode.tokenEnd
        )
        classNode.fields.add(fieldNode)

        if (cstFieldNode.initialValue != null) {
          // need to create method scope to properly resolve everything
          if (fieldNode.isStatic) {
            val stInitMethod = getOrCreateStaticInitialisationMethod(classNode)
            useScope(MethodScope(classScope, stInitMethod)) {
              staticFieldInitialValueMap[fieldNode] = cstFieldNode.initialValue!!.accept(this, fieldNode.type)
            }
          } else {
            fieldInitialValueMap[fieldNode] = useScope(
              MethodScope(
                classScope,
                JavaConstructorImpl(Visibility.PRIVATE, isVarArgs = false, classType, emptyList())
              )
            ) {
              caster.cast(fieldNode.type, cstFieldNode.initialValue!!.accept(this, fieldNode.type))
            }
          }
        }
      }


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

      if (node is ScriptCstNode) {
        // need this to be at the end of this function
        classNode.innerClasses.forEach { innerClassNode ->
          if (innerClassNode is LambdaClassNode) {
            defineLambda(innerClassNode)
          }
        }
      }
      return@useScope classNode
    }

  private fun getOrCreateStaticInitialisationMethod(classNode: ClassNode): MethodNode {
    val m = classNode.methods.find { it.name == JavaMethod.STATIC_INITIALIZATION_BLOCK }
    if (m != null) return m
    val newMethod = SemanticHelper.staticInitialisationMethod(classNode)
    classNode.methods.add(newMethod)
    return newMethod
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
          expression = caster.cast(field.type, initialValue),
          tokenStart = field.tokenStart, tokenEnd = initialValue.tokenEnd
        )
      )
    }
  }

  private fun methodNode(classNode: ClassNode, methodCst: MethodCstNode, classScope: ClassScope): MethodNode {
    val (returnType, asyncReturnType) = resolveReturnType(methodCst)
    val methodNode = toMethodNode(
      classNode, methodCst, methodCst.name,
      returnType, asyncReturnType, classScope.classType
    )
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
        constructorNode.parameters.add(i, MethodParameter(outerClassField.type, outerClassField.name))
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
      val superConstructorMethod = symbolResolver.findMethod(superType, JavaMethod.CONSTRUCTOR_NAME, emptyList())
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

    // because we want to add thisParameter varAssign AFTER outer class fields assignements
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
                  index = i + 1, isFinal = false
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
              variable = SemanticHelper.parameterToLocalVariable(constructorNode, param),
              token = constructorCstNode.token
            ), node = constructorCstNode
          )
        )
      )
    }
    return constructorNode
  }

  private fun toConstructorNode(classNode: ClassNode, methodCst: AbstractMethodCstNode, classType: JavaType) =
    toMethodNode(classNode, methodCst, JavaMethod.CONSTRUCTOR_NAME, JavaType.void, asyncReturnType = null, classType)

  private fun toMethodNode(
    classNode: ClassNode, methodCst: AbstractMethodCstNode, methodName: String,
    returnType: JavaType, asyncReturnType: JavaType?, classType: JavaType
  ): MethodNode {
    val visibility = Visibility.fromTokenType(methodCst.accessNode.visibility)
    val isStatic = methodCst.accessNode.isStatic
    val parameters = mutableListOf<MethodParameter>()
    methodCst.parameters.forEachIndexed { index, methodParameterCstNode ->
      parameters.add(toMethodParameterNode(classNode, visibility, isStatic, index, methodName, methodParameterCstNode))
    }
    return MethodNode(
      name = methodName,
      visibility = visibility,
      returnType = returnType,
      isStatic = isStatic,
      tokenStart = methodCst.tokenStart,
      tokenEnd = methodCst.tokenEnd,
      parameters = parameters,
      ownerClass = classType,
      asyncReturnType = asyncReturnType
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
          lastStatement = ReturnStatementNode(caster.cast(JavaType.Object, lastStatement.expressionNode))
        }
        add(lastStatement)
      }
    } else blockStatements(cstStatements)

    if (!AllPathsReturnVisitor.test(statements)) {
      if (methodeNode.returnType == JavaType.void || methodeNode.asyncReturnType == JavaType.void) {
        statements.add(SemanticHelper.returnVoid(methodeNode))
      } else if (scriptRunMethod) {
        statements.add(SemanticHelper.returnNull(methodeNode))
      } else {
        throw MarcelSemanticException(
          methodeNode.token,
          "Not all paths return a value in method ${methodeNode.ownerClass}.${methodeNode.name}()"
        )
      }
    }
    if (!methodeNode.isAsync) {
      methodeNode.blockStatement.addAll(statements)
    } else {
      // generate doMethod which will have the actual statements
      val doMethodNode = MethodNode(
        name = "_do" + methodeNode.name[0].uppercase() + methodeNode.name.substring(1),
        parameters = methodeNode.parameters,
        visibility = Visibility.INTERNAL,
        returnType = methodeNode.asyncReturnType!!,
        isStatic = methodeNode.isStatic,
        tokenStart = methodeNode.tokenStart,
        tokenEnd = methodeNode.tokenEnd,
        ownerClass = methodeNode.ownerClass
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
      useScope(
        MethodScope(
          ClassScope(symbolResolver, lambdaClassNode.type, null, ImportResolver.DEFAULT_IMPORTS),
          lambdaMethod
        )
      ) { scope ->
        lambdaMethod.blockStatement.statements.apply {
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
            add(ExpressionStatementNode(fCall))
            add(SemanticHelper.returnVoid(lambdaMethod))
          } else {
            add(ReturnStatementNode(fCall))
          }
        }
      }

      // now fill the method with the call to Threadmill
      methodeNode.blockStatement.apply {
        add(
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
  }
}