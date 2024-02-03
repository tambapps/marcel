package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodInnerScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import marcel.lang.lambda.Lambda
import java.util.*

abstract class MarcelBaseSemantic {

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

  protected abstract val symbolResolver: MarcelSymbolResolver
  protected abstract val caster: AstNodeCaster

  val scopeQueue = LinkedList<Scope>()
  protected val currentScope: Scope get() = scopeQueue.peek() // FIFO
  protected val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a method")
  protected val currentInnerMethodScope get() = currentScope as? MethodInnerScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a inner scope")

  protected inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  protected fun newInnerScope() = MethodInnerScope(currentMethodScope)
  protected inline fun <U> useInnerScope(consumer: (MethodInnerScope) -> U)
      = useScope(newInnerScope(), consumer)

  /**
   * Cast method arguments if necessary and transform them to handle varags if necessary
   *
   * @param method the methods
   * @param arguments the arguments
   * @return the list of arguments to call the provided method
   */
  protected fun castedArguments(method: JavaMethod, arguments: List<ExpressionNode>): List<ExpressionNode> {
    if (!method.isVarArgs
      // in case the provider did provide the array
      || symbolResolver.matchesMethod(method, arguments)) return arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }
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

  protected fun fCall(node: CstNode, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode,
                    castType: JavaType? = null): ExpressionNode {
    return fCall(node, owner.type, name, arguments, owner, castType)
  }

  protected fun fCall(node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, node.token)
    return fCall(node, method, arguments, owner, castType)
  }

  protected fun fCall(tokenStart: LexToken, tokenEnd: LexToken, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  protected fun fCall(
    node: CstNode,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null) = fCall(node.tokenStart,
    LexToken.DUMMY, // passing dummy to inform code highlight that this is not a fCall from the real marcel source code
    method, arguments, owner, castType)
  protected fun fCall(
    tokenStart: LexToken,
    tokenEnd: LexToken,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null): ExpressionNode {
    if (method.isAsync &&  (currentScope as? MethodScope)?.isAsync != true) throw MarcelSemanticException(tokenStart, "Method $method is async but was call in a non async context")
    if (owner != null && method.isMarcelStatic) throw MarcelSemanticException(tokenStart, "Method $method is static but was call from an instance")
    if (!method.isAccessibleFrom(currentScope.classType)) {
      throw MarcelSemanticException(tokenStart, "Method $method is not accessible from class" + currentScope.classType)
    }
    val node = FunctionCallNode(method, owner, castedArguments(method, arguments), tokenStart, tokenEnd)
    return if (castType != null) caster.cast(castType, node) else node
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
      lambdaConstructor.blockStatement.statements.add(i + 1, // +1 because first statement should be super call
        ExpressionStatementNode(
          VariableAssignmentNode(
            owner = ThisReferenceNode(lambdaNode.type, lambdaNode.token),
            variable = outerClassField,
            expression = ReferenceNode(variable = LocalVariable(outerClassField.type, outerClassField.name, nbSlots = 1, index = i + 1, isFinal = false),  token = token),
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
  protected fun getInnerOuterReference(token: LexToken, outerLevel: Int) = getOuterLevelReference(currentScope, token, outerLevel - 1)

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
  protected fun createLambdaNode(outerClassNode: ClassNode,
                                 references: List<ReferenceNode>,
                                 lambdaMethodParameters: List<MethodParameter>,
                                 returnType: JavaType,
                                 interfaceType: JavaType,
                                 tokenStart: LexToken,
                                 tokenEnd: LexToken
  ): Triple<ClassNode, MethodNode, NewInstanceNode> {
    val lambdaImplementedInterfaces = listOf(Lambda::class.javaType, interfaceType)
    val lambdaType = symbolResolver.defineType(tokenStart,
      Visibility.INTERNAL, outerClassNode.type, generateLambdaClassName(outerClassNode), JavaType.Object, false, lambdaImplementedInterfaces)

    val lambdaClassNode = ClassNode(
      type = lambdaType,
      visibility = Visibility.INTERNAL,
      forExtensionType = null,
      isStatic = outerClassNode.isStatic,
      isScript = false,
      fileName = outerClassNode.fileName,
      tokenStart,
      tokenEnd
    )
    outerClassNode.innerClasses.add(lambdaClassNode)

    val lambdaConstructor = MethodNode(
      name = JavaMethod.CONSTRUCTOR_NAME,
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
          ExpressionStatementNode(SemanticHelper.superNoArgConstructorCall(lambdaClassNode, symbolResolver)),
          SemanticHelper.returnVoid(lambdaClassNode)
        )
      )
    }
    val constructorArguments = mutableListOf<ExpressionNode>()
    handleLambdaInnerClassFields(lambdaClassNode, lambdaConstructor, constructorArguments, tokenStart)

    if (references.isNotEmpty()) {
      for (reference in references) {
        // add field node
        val fieldNode = FieldNode(reference.type, reference.variable.name, lambdaType, emptyList(), true, Visibility.PRIVATE,
          isStatic = false, tokenStart, tokenEnd)
        symbolResolver.defineField(lambdaType, fieldNode)
        lambdaClassNode.fields.add(fieldNode)
        // ... method parameter
        lambdaConstructor.parameters.add(MethodParameter(reference.type, reference.variable.name))
        // ... and constructor argument
        constructorArguments.add(reference)
      }

      // add field assignments in constructor
      useScope(MethodScope(ClassScope(symbolResolver, lambdaType, null, Scope.DEFAULT_IMPORTS), lambdaConstructor)) { scope ->
        val insertIndex = lambdaConstructor.blockStatement.statements.size - 1 // inserting just before the return void statement (the last statement)
        for (reference in references) {
          lambdaConstructor.blockStatement.statements.add(insertIndex, ExpressionStatementNode(
            VariableAssignmentNode(
              owner = ThisReferenceNode(lambdaClassNode.type, tokenStart),
              variable = lambdaClassNode.fields.find { it.name == reference.variable.name }!!,
              expression = ReferenceNode(variable = scope.findLocalVariable(reference.variable.name)!!, token = tokenStart)
            )
          ))
        }
      }
    }
    lambdaClassNode.methods.add(lambdaConstructor)

    val interfaceMethod = symbolResolver.getInterfaceLambdaMethodOrThrow(interfaceType, tokenStart)
    // define lambda method
    val lambdaMethod = MethodNode(interfaceMethod.name, lambdaMethodParameters.toMutableList(),
      Visibility.PUBLIC, returnType, isStatic = false, tokenStart, tokenEnd, lambdaType)

    lambdaClassNode.methods.add(lambdaMethod)

    return Triple(lambdaClassNode, lambdaMethod, NewInstanceNode(lambdaType, lambdaConstructor, constructorArguments, tokenStart))
  }

  protected fun block(vararg statements: StatementNode): BlockStatementNode {
    return block(statements.toMutableList())
  }
  protected fun block(statements: List<StatementNode>): BlockStatementNode {
    return BlockStatementNode(if (statements is MutableList) statements else statements.toMutableList(),
      statements.firstOrNull()?.tokenStart ?: LexToken.DUMMY, statements.firstOrNull()?.tokenEnd ?: LexToken.DUMMY)
  }
}