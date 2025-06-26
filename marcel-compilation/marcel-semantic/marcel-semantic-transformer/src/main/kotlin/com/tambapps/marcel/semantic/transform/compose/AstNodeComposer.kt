package com.tambapps.marcel.semantic.transform.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.processor.AbstractMarcelSemantic
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.processor.compose.StatementsComposer
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.scope.Scope
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation
import com.tambapps.marcel.semantic.symbol.type.JavaAnnotationType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.processor.visitor.AllPathsReturnVisitor
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.symbol.type.Nullness
import java.util.*

/**
 * Abstract class providing handy methods to constructor AST nodes in an elegant way
 */
abstract class AstNodeComposer(
  val tokenStart: LexToken,
  val tokenEnd: LexToken,
  scopeQueue: LinkedList<Scope> = LinkedList<Scope>(),
) : AbstractMarcelSemantic(scopeQueue, NullSafetyMode.DISABLED) {
  protected fun parameter(type: JavaType, name: String) = MethodParameter(type, Nullness.UNKNOWN, name)

  protected fun signature(
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    nullness: Nullness,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    isConstructor: Boolean = false
  ): MarcelMethod {
    return MarcelMethodImpl(
      currentScope.classType,
      visibility,
      name,
      nullness,
      parameters,
      returnType,
      isDefault,
      isAbstract,
      isStatic,
      isConstructor
    )
  }

  protected fun annotationNode(
    type: JavaAnnotationType,
    attributes: List<JavaAnnotation.Attribute> = emptyList()
  ): AnnotationNode {
    return AnnotationNode(type, attributes, tokenStart, tokenEnd)
  }

  protected inline fun constructorNode(
    classNode: ClassNode,
    visibility: Visibility = Visibility.PUBLIC,
    parameters: List<MethodParameter> = emptyList(),
    statementsSupplier: StatementsComposer.() -> Unit
  ) = methodNode(
    classNode.type, visibility, MarcelMethod.CONSTRUCTOR_NAME, Nullness.NOT_NULL, parameters, JavaType.void, isStatic = false
  ) {
    // super method call
    stmt(superNoArgConstructorCall(classNode, symbolResolver))
    statementsSupplier.invoke(this)

    // return void because constructor
    returnStmt(VoidExpressionNode(tokenStart))
  }

  protected inline fun methodNode(
    ownerClass: JavaType = currentScope.classType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    nullness: Nullness,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isStatic: Boolean = false,
    annotations: List<AnnotationNode> = emptyList(),
    statementsSupplier: StatementsComposer.() -> Unit
  ): MethodNode {
    val methodNode = MethodNode(
      name,
      nullness,
      parameters.toMutableList(),
      visibility,
      returnType,
      isStatic,
      tokenStart,
      tokenEnd,
      ownerClass
    )
    methodNode.annotations.addAll(annotations)
    val statements = methodNode.blockStatement.statements

    useScope(MethodScope(ClassScope(symbolResolver, ownerClass, null, ImportResolver.DEFAULT_IMPORTS), methodNode)) {
      val statementComposer = newStatementsComposer(statements)
      statementsSupplier.invoke(statementComposer) // it will directly add the statements on the method's statements
    }

    if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
      statements.add(returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun addStatements(methodNode: MethodNode, statementsSupplier: StatementsComposer.() -> Unit): MethodNode {
    val statements = methodNode.blockStatement.statements
    useScope(MethodScope(ClassScope(symbolResolver, methodNode.ownerClass, null, ImportResolver.DEFAULT_IMPORTS), methodNode)) {
      val statementComposer = newStatementsComposer(statements)
      statementsSupplier.invoke(statementComposer) // it will directly add the statements on the method's statements
    }

    if (!AllPathsReturnVisitor.test(statements) && methodNode.returnType == JavaType.void) {
      statements.add(returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun addField(
    classNode: ClassNode,
    fieldNode: FieldNode,
    defaultValue: ExpressionNode? = null
  ) {
    classNode.fields.add(fieldNode)
    if (defaultValue != null) {
      classNode.constructors.forEach {
        addStatementLast(
          ExpressionStatementNode(VariableAssignmentNode(
            variable = fieldNode,
            expression = cast(fieldNode.type, defaultValue),
            owner = ThisReferenceNode(currentScope.classType, tokenStart),
            tokenStart = tokenStart,
            tokenEnd = tokenEnd
          )
          ),
          it.blockStatement
        )
      }
    }
  }

  protected fun fieldNode(
    type: JavaType,
    nullness: Nullness,
    name: String, owner: JavaType = currentScope.classType,
    annotations: List<AnnotationNode> = emptyList(),
    visibility: Visibility = Visibility.PRIVATE,
    isFinal: Boolean = false,
    isStatic: Boolean = false
  ): FieldNode {
    return FieldNode(type, name, owner, nullness, annotations.toMutableList(), isFinal, visibility, isStatic, tokenStart, tokenEnd)
  }

  fun newLambda(
    classNode: ClassNode,
    parameters: List<MethodParameter>, returnType: JavaType, interfaceType: JavaType,
    lambdaBodyStatementComposerFunc: StatementsComposer.() -> Unit
  ): NewInstanceNode {

    val (lambdaClassNode, lambdaMethod, newInstanceNode) = createLambdaNode(
      outerClassNode = classNode,
      references = emptyList(),
      lambdaMethodParameters = parameters,
      returnType = returnType,
      interfaceType = interfaceType,
      tokenStart = tokenStart,
      tokenEnd = tokenEnd
    )

    val statements = lambdaMethod.blockStatement.statements
    useScope(MethodScope(ClassScope(symbolResolver, lambdaClassNode.type, null, ImportResolver.DEFAULT_IMPORTS), lambdaMethod)) {
      val statementComposer = newStatementsComposer(statements)
      lambdaBodyStatementComposerFunc.invoke(statementComposer)
      if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
        statements.add(returnVoid(lambdaMethod))
      }
    }
    return newInstanceNode
  }



  /**
   * Add statement last in the block, but before the return instruction if any
   *
   * @param statement the statement to add
   * @param block the statement block
   */
  fun addStatementLast(statement: StatementNode, block: BlockStatementNode) {
    addStatementLast(statement, block.statements)
  }

  private fun addStatementLast(statement: StatementNode, statements: MutableList<StatementNode>) {
    if (statements.last() is ReturnStatementNode) {
      statements.add(statements.lastIndex, statement)
    } else statements.add(statement)
  }

  fun newStatementsComposer(statements: MutableList<StatementNode>) = StatementsComposer(scopeQueue, this, nullSafetyMode, symbolResolver, statements, tokenStart, tokenEnd)
}