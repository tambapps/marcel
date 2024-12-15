package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.compose.StatementsComposer
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.exception.MemberNotVisibleException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodInnerScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import marcel.lang.lambda.CharLambda1
import marcel.lang.lambda.DoubleLambda1
import marcel.lang.lambda.FloatLambda1
import marcel.lang.lambda.IntLambda1
import marcel.lang.lambda.Lambda
import marcel.lang.lambda.Lambda1
import marcel.lang.lambda.Lambda10
import marcel.lang.lambda.Lambda2
import marcel.lang.lambda.Lambda3
import marcel.lang.lambda.Lambda4
import marcel.lang.lambda.Lambda5
import marcel.lang.lambda.Lambda6
import marcel.lang.lambda.Lambda7
import marcel.lang.lambda.Lambda8
import marcel.lang.lambda.Lambda9
import marcel.lang.lambda.LongLambda1
import marcel.lang.runtime.CharSequenceIterator
import marcel.util.primitives.iterators.CharIterator
import marcel.util.primitives.iterators.DoubleIterator
import marcel.util.primitives.iterators.FloatIterator
import marcel.util.primitives.iterators.IntIterator
import marcel.util.primitives.iterators.LongIterator
import java.util.*

abstract class MarcelSemanticGenerator(
  val scopeQueue: LinkedList<Scope> = LinkedList<Scope>()
) {

  /**
   * Object providing constants for class outer class levels.
   */
  object ClassOuterLevels {
    /**
     * Self level. This level doesn't actually exist in Java as this is just "this".
     */
    const val THIS = -1

    /**
     * Outer level. References the field "this$0"
     */
    const val OUTER = 0

    // OUTER.OUTER would be 1, and so on
  }

  abstract val symbolResolver: MarcelSymbolResolver
  abstract val caster: AstNodeCaster

  protected val currentScope: Scope get() = scopeQueue.peek() // FIFO
  protected val currentMethodScope
    get() = currentScope as? MethodScope ?: throw MarcelSemanticException(
      LexToken.DUMMY,
      "Not in a method"
    )
  protected val currentInnerMethodScope
    get() = currentScope as? MethodInnerScope ?: throw MarcelSemanticException(
      LexToken.DUMMY,
      "Not in a inner scope"
    )

  inline fun <T : Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  fun newInnerScope() = MethodInnerScope(currentMethodScope)
  inline fun <U> useInnerScope(consumer: (MethodInnerScope) -> U) = useScope(newInnerScope(), consumer)

  fun superNoArgConstructorCall(classNode: ClassNode, symbolResolver: MarcelSymbolResolver): SuperConstructorCallNode {
    val superConstructorMethod =
      symbolResolver.findMethodOrThrow(classNode.superType, MarcelMethod.CONSTRUCTOR_NAME, emptyList(), classNode.token)
    return SuperConstructorCallNode(
      classNode.superType,
      superConstructorMethod,
      emptyList(),
      classNode.tokenStart,
      classNode.tokenEnd
    )
  }

  fun noArgConstructor(
    classNode: ClassNode,
    symbolResolver: MarcelSymbolResolver,
    visibility: Visibility = Visibility.PUBLIC
  ): MethodNode {
    val defaultConstructorNode = MethodNode(
      MarcelMethod.CONSTRUCTOR_NAME,
      mutableListOf(),
      visibility,
      JavaType.void,
      false,
      classNode.tokenStart,
      classNode.tokenEnd,
      classNode.type
    )
    defaultConstructorNode.blockStatement.addAll(
      listOf(
        ExpressionStatementNode(superNoArgConstructorCall(classNode, symbolResolver)),
        ReturnStatementNode(
          VoidExpressionNode(defaultConstructorNode.token),
          defaultConstructorNode.tokenStart,
          defaultConstructorNode.tokenEnd
        )
      )
    )
    return defaultConstructorNode
  }

  /**
   * Cast method arguments if necessary and transform them to handle varags if necessary
   *
   * @param method the methods
   * @param arguments the arguments
   * @return the list of arguments to call the provided method
   */
  protected fun castedArguments(method: MarcelMethod, arguments: List<ExpressionNode>): List<ExpressionNode> {
    if (!method.isVarArgs
      // in case the provider did provide the array
      || symbolResolver.matchesMethod(method, arguments)
    ) return arguments.mapIndexed { index, expressionNode ->
      caster.cast(
        method.parameters[index].type,
        expressionNode
      )
    }
    val castedArguments = mutableListOf<ExpressionNode>()

    var i = 0
    while (i < method.parameters.size - 1) {
      castedArguments.add(
        caster.cast(method.parameters[i].type, arguments[i])
      )
      i++
    }
    val varArgType = method.varArgType
    val arrayArgs = mutableListOf<ExpressionNode>()
    while (i < arguments.size) {
      arrayArgs.add(caster.cast(varArgType, arguments[i]))
      i++
    }
    castedArguments.add(ArrayNode(arrayArgs, LexToken.DUMMY, LexToken.DUMMY, method.varArgsType))
    return castedArguments
  }

  fun fCall(
    node: CstNode, name: String, arguments: List<ExpressionNode>,
    owner: ExpressionNode,
    castType: JavaType? = null
  ): ExpressionNode {
    return fCall(node, owner.type, name, arguments, owner, castType)
  }

  fun fCall(
    node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, node.token)
    return fCall(node, method, arguments, owner, castType)
  }

  fun fCall(
    tokenStart: LexToken, tokenEnd: LexToken, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  fun fCall(
    node: CstNode,
    method: MarcelMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null
  ) = fCall(
    node.tokenStart,
    LexToken.DUMMY, // passing dummy to inform code highlight that this is not a fCall from the real marcel source code
    method, arguments, owner, castType
  )

  fun fCall(
    tokenStart: LexToken,
    tokenEnd: LexToken,
    method: MarcelMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null
  ): ExpressionNode {
    if (method.isAsync && (currentScope as? MethodScope)?.isAsync != true) throw MarcelSemanticException(
      tokenStart,
      "Method $method is async but was call in a non async context"
    )
    if (owner != null && method.isMarcelStatic) throw MarcelSemanticException(
      tokenStart,
      "Method $method is static but was call from an instance"
    )
    if (!method.isVisibleFrom(currentScope.classType)) {
      throw
      MemberNotVisibleException(tokenStart, method, currentScope.classType)
    }
    val node = FunctionCallNode(
      method,
      owner,
      castedArguments(method, arguments),
      tokenStart,
      tokenEnd
    )
    return if (castType != null) caster.javaCast(castType, node) else node
  }

  fun forInArrayNode(
    node: CstNode,
    forScope: MethodScope,
    inNode: ExpressionNode,
    iVar: LocalVariable,
    forVariable: LocalVariable,
    statementNode: StatementNode
  ): ForStatementNode {
    return forInArrayNode(node, forScope, inNode, iVar, forVariable) { statementNode }
  }

  inline fun forInArrayNode(
    node: CstNode, forScope: MethodScope, inNode: ExpressionNode, iVar: LocalVariable, forVariable: LocalVariable,
    // lambda because we want the body to be semantically checked AFTER we created the iteratorVariable
    bodyCreator: () -> StatementNode
  ) = forInArrayNode(node.tokenStart, node.tokenEnd, forScope, inNode, iVar, forVariable, bodyCreator)

  inline fun forInArrayNode(
    tokenStart: LexToken, tokenEnd: LexToken, forScope: MethodScope, inNode: ExpressionNode, iVar: LocalVariable, forVariable: LocalVariable,
    // lambda because we want the body to be semantically checked AFTER we created the iteratorVariable
    bodyCreator: () -> StatementNode
  ): ForStatementNode {
    val iRef = ReferenceNode(variable = iVar, token = tokenStart)
    val arrayVar = forScope.addLocalVariable(inNode.type)
    val arrayRef = ReferenceNode(variable = arrayVar, token = tokenStart)

    // init variable
    val initStatement = BlockStatementNode(
      mutableListOf(
        ExpressionStatementNode(VariableAssignmentNode(localVariable = arrayVar, expression = inNode, tokenStart, tokenEnd)),
        ExpressionStatementNode(
          VariableAssignmentNode(
            localVariable = iVar,
            expression = IntConstantNode(tokenStart, 0),
            tokenStart, tokenEnd
          )
        )
      ), tokenStart, tokenEnd
    )

    // i < array.length
    val condition = LtNode(
      leftOperand = iRef,
      rightOperand = ReferenceNode(owner = arrayRef, symbolResolver.findField(arrayVar.type, "length")!!, tokenStart)
    )

    // i++
    val iteratorStatement = ExpressionStatementNode(IncrNode(tokenStart, iVar, 1, JavaType.int, false))

    // body
    val body = BlockStatementNode(
      mutableListOf(
        ExpressionStatementNode(
          VariableAssignmentNode(
            localVariable = forVariable, expression = caster.cast(
              forVariable.type,
              ArrayAccessNode(owner = arrayRef, indexNode = iRef, tokenStart, tokenEnd)
            ), tokenStart, tokenEnd
          )
        ),
        bodyCreator.invoke()
      ), tokenStart, tokenEnd
    )

    val forNode = ForStatementNode(tokenStart, tokenEnd, initStatement, condition, iteratorStatement, body)

    forScope.freeLocalVariable(iVar.name)
    forScope.freeLocalVariable(arrayVar.name)
    return forNode
  }

  fun forInIteratorNode(
    node: CstNode, forScope: MethodScope, variable: LocalVariable, inNode: ExpressionNode,
    bodtStmt: StatementNode
  ) = forInIteratorNode(node, forScope, variable, inNode) { bodtStmt }

  inline fun forInIteratorNode(
    node: CstNode, forScope: MethodScope, variable: LocalVariable, inNode: ExpressionNode,
    // lambda because we want the body to be semantically checked AFTER we created the iteratorVariable
    bodyCreator: () -> StatementNode
  ): ForInIteratorStatementNode {
    val iteratorExpression = when {
      inNode.type.implements(Iterable::class.javaType) -> fCall(node, inNode.type, "iterator", emptyList(), inNode)
      inNode.type.implements(Iterator::class.javaType) -> inNode
      inNode.type.implements(CharSequence::class.javaType) -> NewInstanceNode(
        CharSequenceIterator::class.javaType,
        symbolResolver.findMethod(CharSequenceIterator::class.javaType, MarcelMethod.CONSTRUCTOR_NAME, listOf(inNode))!!,
        listOf(inNode),
        node.token
      )

      else -> throw MarcelSemanticException(node.token, "Cannot iterate over an expression of type ${inNode.type}")
    }
    val iteratorExpressionType = iteratorExpression.type
    return forScope.useTempLocalVariable(iteratorExpressionType) { iteratorVariable ->
      val (nextMethodOwnerType, nextMethodName) = if (IntIterator::class.javaType.isAssignableFrom(
          iteratorExpressionType
        )
      ) Pair(IntIterator::class.javaType, "nextInt")
      else if (LongIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(
        LongIterator::class.javaType,
        "nextLong"
      )
      else if (FloatIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(
        FloatIterator::class.javaType,
        "nextFloat"
      )
      else if (DoubleIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(
        DoubleIterator::class.javaType,
        "nextDouble"
      )
      else if (CharIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(
        CharIterator::class.javaType, "nextChar"
      )
      else if (Iterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(Iterator::class.javaType, "next")
      else throw UnsupportedOperationException("wtf")

      val iteratorVarReference = ReferenceNode(variable = iteratorVariable, token = node.token)

      val nextMethod = symbolResolver.findMethodOrThrow(nextMethodOwnerType, nextMethodName, emptyList())
      // cast to fit the declared variable type
      val nextMethodCall = caster.cast(
        variable.type,
        fCall(node = node, method = nextMethod, arguments = emptyList(), owner = iteratorVarReference)
      )
      ForInIteratorStatementNode(
        node,
        variable,
        iteratorVariable,
        iteratorExpression,
        nextMethodCall,
        bodyCreator.invoke()
      )
    }
  }

  fun staticInitialisationMethod(classNode: ClassNode): MethodNode {
    return MethodNode(
      ownerClass = classNode.type,
      name = MarcelMethod.STATIC_INITIALIZATION_BLOCK,
      parameters = mutableListOf(),
      visibility = Visibility.PRIVATE,
      isStatic = true,
      returnType = JavaType.void,
      tokenStart = classNode.tokenStart,
      tokenEnd = classNode.tokenEnd
    ).apply {
      // all functions should finish with a return statement, even the void ones
      blockStatement.add(ReturnStatementNode(null, tokenStart, tokenEnd))
    }
  }

  inline fun compose(methodNode: MethodNode, composer: StatementsComposer.(MethodScope) -> Unit): MethodNode {
    return compose(methodNode, MethodScope(ClassScope(symbolResolver, methodNode.ownerClass, null, ImportResolver.DEFAULT_IMPORTS), methodNode), composer)
  }
  inline fun compose(methodNode: MethodNode, methodScope: MethodScope, composer: StatementsComposer.(MethodScope) -> Unit): MethodNode {
    useScope(methodScope) { scope ->
      val statementComposer = StatementsComposer(scopeQueue, caster, symbolResolver, methodNode.blockStatement.statements, methodNode.tokenStart, methodNode.tokenEnd)
      composer.invoke(statementComposer, scope)
    }
    return methodNode
  }

  fun returnVoid(node: AstNode) = ReturnStatementNode(VoidExpressionNode(node.token), node.tokenStart, node.tokenEnd)

  fun getLambdaType(node: AstNode, lambdaParameters: List<MethodParameter>): JavaType {
    val returnType = JavaType.Object

    return when (lambdaParameters.size) {
      0 -> JavaType.of(Lambda1::class.java).withGenericTypes(JavaType.Object)
      1 -> when (lambdaParameters.first().type) {
        JavaType.int -> JavaType.of(IntLambda1::class.java).withGenericTypes(returnType)
        JavaType.long -> JavaType.of(LongLambda1::class.java).withGenericTypes(returnType)
        JavaType.float -> JavaType.of(FloatLambda1::class.java).withGenericTypes(returnType)
        JavaType.double -> JavaType.of(DoubleLambda1::class.java).withGenericTypes(returnType)
        JavaType.char -> JavaType.of(CharLambda1::class.java).withGenericTypes(returnType)
        else -> JavaType.of(Lambda1::class.java).withGenericTypes(lambdaParameters.first().type.objectType, returnType)
      }

      2 -> JavaType.of(Lambda2::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      3 -> JavaType.of(Lambda3::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      4 -> JavaType.of(Lambda4::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      5 -> JavaType.of(Lambda5::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      6 -> JavaType.of(Lambda6::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      7 -> JavaType.of(Lambda7::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      8 -> JavaType.of(Lambda8::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      9 -> JavaType.of(Lambda9::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      10 -> JavaType.of(Lambda10::class.java).withGenericTypes(lambdaParameters.map { it.type } + returnType)
      else -> throw MarcelSemanticException(node.token, "Doesn't handle lambdas with more than 10 parameters")
    }
  }

  protected fun generateLambdaClassName(lambdaOuterClassNode: ClassNode): String {
    return (
        if (currentMethodScope.method.isConstructor) "init"
        else currentMethodScope.method.name
        ) + "_lambda" + (lambdaOuterClassNode.innerClasses.count { it is LambdaClassNode } + 1)
  }

  protected fun generateOutClassFields(classType: JavaType, classNode: ClassNode): List<FieldNode> {
    val fields = mutableListOf<FieldNode>()
    if (!classNode.isStatic && classType.outerTypeName != null) {
      // java generates fields to reference outer class(es) from inner class. So does marcel.
      var outerLevel = 0
      var levelType: JavaType? = classType.outerTypeName?.let { symbolResolver.of(it, token = classNode.token) }
      while (levelType != null) {
        val outerFieldName = "this$$outerLevel"
        val fieldNode = FieldNode(
          type = levelType,
          name = outerFieldName,
          owner = classNode.type,
          annotations = emptyList(),
          isFinal = true,
          visibility = Visibility.INTERNAL,
          isStatic = false,
          isSynthetic = true,
          tokenStart = classNode.tokenStart,
          tokenEnd = classNode.tokenEnd
        )
        fields.add(fieldNode)
        classNode.fields.add(fieldNode)
        symbolResolver.defineField(classType, fieldNode)

        outerLevel++
        levelType = levelType.outerTypeName?.let { symbolResolver.of(it, emptyList(), classNode.token) }
      }
    }
    return fields
  }

  protected fun handleLambdaInnerClassFields(
    lambdaNode: ClassNode,
    lambdaConstructor: MethodNode,
    constructorArguments: MutableList<ExpressionNode>,
    token: LexToken,
  ) {
    val outerClassFields = generateOutClassFields(lambdaNode.type, lambdaNode)
    for (i in outerClassFields.indices) {
      // if we're here we know the context is not static as the above method only generates fields if it is not static
      val outerClassField = outerClassFields[i]
      // adding at the beginning
      lambdaConstructor.parameters.add(i, MethodParameter(outerClassField.type, outerClassField.name))
      val (outerLevel, _) = outerLevel(token, lambdaNode.type, outerClassField.type)
        ?: throw MarcelSemanticException(token, "Lambda cannot be generated in this context")

      val argument = getInnerOuterReference(token, outerLevel)
        ?: throw MarcelSemanticException(token, "Lambda cannot be generated in this context")
      constructorArguments.add(i, argument)
      lambdaConstructor.blockStatement.statements.add(
        i + 1, // +1 because first statement should be super call
        ExpressionStatementNode(
          VariableAssignmentNode(
            owner = ThisReferenceNode(lambdaNode.type, lambdaNode.token),
            variable = outerClassField,
            expression = ReferenceNode(
              variable = LocalVariable(
                outerClassField.type,
                outerClassField.name,
                nbSlots = 1,
                index = i + 1,
                isFinal = false
              ), token = token
            ),
            tokenStart = token,
            tokenEnd = token,
          )
        )
      )
    }
  }

  /**
   * Get the reference to pass to an inner class constructor for the provided outerLevel.
   * E.g. for the level OUTER, it would give THIS. For the level OUTER.OUTER, it would give THIS.OUTER
   *
   * @param token the token in case of error
   * @param outerLevel the outer level
   * @return the reference to pass to an inner class constructor for the provided outerLevel
   */
  protected fun getInnerOuterReference(token: LexToken, outerLevel: Int) =
    getOuterLevelReference(currentScope, token, outerLevel - 1)

  /**
   * Get the reference of given outer class level
   *
   * @param scope the scope from which to extract types/fields
   * @param token the token in case of error
   * @param outerLevel the outer level
   * @return the reference of given outer class level
   */
  protected fun getOuterLevelReference(scope: Scope, token: LexToken, outerLevel: Int): ExpressionNode? {
    val thisNode = ThisReferenceNode(scope.classType, token)
    return if (outerLevel == ClassOuterLevels.THIS) thisNode
    // this name pattern is Java's outer class references pattern
    else scope.findField("this$$outerLevel")?.let { ReferenceNode(owner = thisNode, variable = it, token = token) }
  }

  // -1 means self, 0 means outer, 1 means outer.outer and so on
  // this is in order to be coherent with this$0 which corresponds to the outer, and so on
  protected fun outerLevel(token: LexToken, innerClass: JavaType, outerClass: JavaType): Pair<Int, JavaType>? {
    var outerLevel = ClassOuterLevels.THIS
    var levelType: JavaType? = innerClass
    while (levelType != null && !outerClass.isAssignableFrom(levelType)) {
      outerLevel++
      levelType = levelType.outerTypeName?.let { symbolResolver.of(it, emptyList(), token) }
    }
    return if (levelType == null) null
    else Pair(outerLevel, levelType)
  }

  /**
   * Create lambda node and returns the lambda node, the lambda method node and the new instance node.
   *
   * Note that the lambda method node statements are not filled. It is the caller's responsibility to fill it
   *
   * @param outerClassNode the outer class node
   * @param references the references used in this lambda. They will be fields of the lambda and passed to the lambda's constructor
   * @param lambdaMethodParameters the lambda method parameters
   * @param returnType the lambda return type
   * @param interfaceType the interface the lambda should implement
   * @param tokenStart the tokenStart
   * @param tokenEnd the tokenEnd
   * @return the lambda node, the lambda method node and the new instance node
   */
  protected fun createLambdaNode(
    outerClassNode: ClassNode,
    references: List<ReferenceNode>,
    lambdaMethodParameters: List<MethodParameter>,
    returnType: JavaType,
    interfaceType: JavaType,
    tokenStart: LexToken,
    tokenEnd: LexToken
  ): Triple<ClassNode, MethodNode, NewInstanceNode> {
    val lambdaImplementedInterfaces = listOf(Lambda::class.javaType, interfaceType)
    val lambdaType = symbolResolver.defineType(
      tokenStart,
      Visibility.INTERNAL,
      outerClassNode.type,
      generateLambdaClassName(outerClassNode),
      JavaType.Object,
      false,
      lambdaImplementedInterfaces
    )

    val lambdaClassNode = ClassNode(
      type = lambdaType,
      visibility = Visibility.INTERNAL,
      forExtensionType = null,
      isStatic = outerClassNode.isStatic,
      isScript = false,
      isEnum = false,
      fileName = outerClassNode.fileName,
      tokenStart,
      tokenEnd
    )
    outerClassNode.innerClasses.add(lambdaClassNode)

    val lambdaConstructor = MethodNode(
      name = MarcelMethod.CONSTRUCTOR_NAME,
      visibility = Visibility.INTERNAL,
      returnType = JavaType.void,
      isStatic = false,
      tokenStart = tokenStart,
      tokenEnd = tokenEnd,
      parameters = mutableListOf(),
      ownerClass = lambdaType
    ).apply {
      blockStatement.addAll(
        listOf(
          ExpressionStatementNode(superNoArgConstructorCall(lambdaClassNode, symbolResolver)),
          returnVoid(lambdaClassNode)
        )
      )
    }
    val constructorArguments = mutableListOf<ExpressionNode>()
    handleLambdaInnerClassFields(lambdaClassNode, lambdaConstructor, constructorArguments, tokenStart)

    if (references.isNotEmpty()) {
      for (reference in references) {
        // add field node
        val fieldNode = FieldNode(
          reference.type, reference.variable.name, lambdaType, emptyList(), true, Visibility.PRIVATE,
          isStatic = false, tokenStart, tokenEnd
        )
        symbolResolver.defineField(lambdaType, fieldNode)
        lambdaClassNode.fields.add(fieldNode)
        // ... method parameter
        lambdaConstructor.parameters.add(MethodParameter(reference.type, reference.variable.name))
        // ... and constructor argument
        constructorArguments.add(reference)
      }

      // add field assignments in constructor
      useScope(
        MethodScope(
          ClassScope(symbolResolver, lambdaType, null, ImportResolver.DEFAULT_IMPORTS),
          lambdaConstructor
        )
      ) { scope ->
        val insertIndex =
          lambdaConstructor.blockStatement.statements.size - 1 // inserting just before the return void statement (the last statement)
        for (reference in references) {
          lambdaConstructor.blockStatement.statements.add(
            insertIndex, ExpressionStatementNode(
              VariableAssignmentNode(
                owner = ThisReferenceNode(lambdaClassNode.type, tokenStart),
                variable = lambdaClassNode.fields.find { it.name == reference.variable.name }!!,
                expression = ReferenceNode(
                  variable = scope.findLocalVariable(reference.variable.name)!!,
                  token = tokenStart
                )
              )
            )
          )
        }
      }
    }
    lambdaClassNode.methods.add(lambdaConstructor)

    val interfaceMethod = symbolResolver.getInterfaceLambdaMethodOrThrow(interfaceType, tokenStart)
    // define lambda method
    val lambdaMethod = MethodNode(
      interfaceMethod.name, lambdaMethodParameters.toMutableList(),
      Visibility.PUBLIC, returnType, isStatic = false, tokenStart, tokenEnd, lambdaType
    )

    lambdaClassNode.methods.add(lambdaMethod)

    return Triple(
      lambdaClassNode,
      lambdaMethod,
      NewInstanceNode(lambdaType, lambdaConstructor, constructorArguments, tokenStart)
    )
  }

  protected fun block(vararg statements: StatementNode): BlockStatementNode {
    return block(statements.toMutableList())
  }

  protected fun block(statements: List<StatementNode>): BlockStatementNode {
    return BlockStatementNode(
      if (statements is MutableList) statements else statements.toMutableList(),
      statements.firstOrNull()?.tokenStart ?: LexToken.DUMMY, statements.firstOrNull()?.tokenEnd ?: LexToken.DUMMY
    )
  }
}