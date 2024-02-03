package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.MarcelBaseSemantic
import com.tambapps.marcel.semantic.SemanticHelper
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.JavaMethodImpl
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaAnnotation
import com.tambapps.marcel.semantic.type.JavaAnnotationType
import com.tambapps.marcel.semantic.type.JavaArrayType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor

/**
 * Abstract class providing handy methods to constructor AST nodes in an elegant way
 */
abstract class AstNodeComposer: MarcelBaseSemantic() {

  protected fun parameter(type: JavaType, name: String) = MethodParameter(type, name)

  protected fun signature(
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    isConstructor: Boolean = false
  ): JavaMethod {
    return JavaMethodImpl(currentScope.classType, visibility, name, parameters, returnType, isDefault, isAbstract, isStatic, isConstructor)
  }

  protected fun annotationNode(type: JavaAnnotationType, attributes: List<JavaAnnotation.Attribute> = emptyList()): AnnotationNode {
    return AnnotationNode(type, attributes, LexToken.DUMMY, LexToken.DUMMY)
  }

  protected inline fun constructorNode(
    classNode: ClassNode,
    visibility: Visibility = Visibility.PUBLIC,
    parameters: List<MethodParameter> = emptyList(),
    statementsSupplier: StatementsComposer.() -> Unit
  ) = methodNode(classNode.type, visibility, JavaMethod.CONSTRUCTOR_NAME, parameters, JavaType.void, isDefault = false,
    isAbstract = false, isStatic = false, emptyList()) {
    // super method call
    stmt(SemanticHelper.superNoArgConstructorCall(classNode, symbolResolver))
    statementsSupplier.invoke(this)

    // return void because constructor
    returnStmt(VoidExpressionNode(LexToken.DUMMY))
  }

  protected inline fun methodNode(
    ownerClass: JavaType = currentScope.classType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    annotations: List<AnnotationNode> = emptyList(),
    statementsSupplier: StatementsComposer.() -> Unit
  ): MethodNode {
    val methodNode = MethodNode(name, parameters.toMutableList(), visibility, returnType, isStatic, LexToken.DUMMY, LexToken.DUMMY, ownerClass)
    methodNode.annotations.addAll(annotations)
    val statements = methodNode.blockStatement.statements

    useScope(MethodScope(ClassScope(symbolResolver, ownerClass, null, Scope.DEFAULT_IMPORTS), methodNode)) {
      val statementComposer = StatementsComposer(statements)
      statementsSupplier.invoke(statementComposer) // it will directly add the statements on the method's statements
    }

    if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
      statements.add(SemanticHelper.returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun addStatements(methodNode: MethodNode, statementsSupplier: StatementsComposer.() -> Unit): MethodNode {
    val statements = methodNode.blockStatement.statements
    useScope(MethodScope(ClassScope(symbolResolver, methodNode.ownerClass, null, Scope.DEFAULT_IMPORTS), methodNode)) {
      val statementComposer = StatementsComposer(statements)
      statementsSupplier.invoke(statementComposer) // it will directly add the statements on the method's statements
    }

    if (!AllPathsReturnVisitor.test(statements) && methodNode.returnType == JavaType.void) {
      statements.add(SemanticHelper.returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun addField(classNode: ClassNode, fieldNode: FieldNode, defaultValue: ExpressionNode? = null) {
    classNode.fields.add(fieldNode)
    if (defaultValue != null) {
      classNode.constructors.forEach {
        SemanticHelper.addStatementLast(
          ExpressionStatementNode(varAssignExpr(fieldNode, defaultValue, thisRef())),
          it.blockStatement
        )
     }
    }
  }

  protected fun fieldNode(type: JavaType, name: String, owner: JavaType = currentScope.classType,
                          annotations: List<AnnotationNode> = emptyList(),
                          visibility: Visibility = Visibility.PRIVATE,
                          isFinal: Boolean = false,
                          isStatic: Boolean = false): FieldNode {
    return FieldNode(type, name, owner, annotations, isFinal, visibility, isStatic, LexToken.DUMMY, LexToken.DUMMY)
  }

  protected fun superRef() = SuperReferenceNode(currentScope.classType.superType!!, LexToken.DUMMY)
  protected fun ref(field: MarcelField,
                    owner: ExpressionNode? = if (field.isMarcelStatic) null else ThisReferenceNode(field.owner, LexToken.DUMMY)): ReferenceNode {
    return ReferenceNode(
      owner = owner,
      variable = field,
      token = LexToken.DUMMY
    )
  }

  // 0 is outer, 1 is outer of outer, and so on...
  protected fun outerRef(level: Int = 0) = ref(currentMethodScope.findField("this$$level")!!)

  protected fun cast(expr: ExpressionNode, type: JavaType): ExpressionNode = caster.cast(type, expr)

  protected fun ref(methodParameter: MethodParameter) = ReferenceNode(owner = null,
    variable = currentMethodScope.findLocalVariable(methodParameter.name)!!, token = LexToken.DUMMY)
  protected fun ref(lv: LocalVariable) = ReferenceNode(owner = null, variable = lv, token = LexToken.DUMMY)

  protected fun string(value: String) = StringConstantNode(value, LexToken.DUMMY, LexToken.DUMMY)
  protected fun string(parts: List<ExpressionNode>) = StringNode(parts, LexToken.DUMMY, LexToken.DUMMY)

  protected fun array(asType: JavaArrayType, vararg elements: ExpressionNode): ArrayNode {
    return array(asType, elements.toMutableList())
  }

  protected fun array(arrayType: JavaArrayType, elements: List<ExpressionNode>): ArrayNode {
    return ArrayNode(if (elements is MutableList) elements else elements.toMutableList(),
      LexToken.DUMMY, LexToken.DUMMY, arrayType)
  }
  protected fun fCall(name: String, arguments: List<ExpressionNode>,
                      owner: ExpressionNode,
                      castType: JavaType? = null): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(owner.type, name, arguments, LexToken.DUMMY)
    return fCall(LexToken.DUMMY, LexToken.DUMMY, method, arguments, owner, castType)
  }
  protected fun fCall(method: JavaMethod, arguments: List<ExpressionNode>,
                      owner: ExpressionNode,
                      castType: JavaType? = null): ExpressionNode {
    return fCall(LexToken.DUMMY, LexToken.DUMMY, method, arguments, owner, castType)
  }
  protected fun fCall(name: String, arguments: List<ExpressionNode>,
                      ownerType: JavaType,
                      castType: JavaType? = null): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, LexToken.DUMMY)
    return fCall(LexToken.DUMMY, LexToken.DUMMY, method, arguments, null, castType)
  }

  protected fun constructorCall(method: JavaMethod, arguments: List<ExpressionNode>): NewInstanceNode {
    return NewInstanceNode(method.ownerClass, method, castedArguments(method, arguments), LexToken.DUMMY)
  }
  protected fun thisRef() = ThisReferenceNode(currentScope.classType, LexToken.DUMMY)

  protected fun bool(b: Boolean) = BoolConstantNode(LexToken.DUMMY, b)
  protected fun int(i: Int) = IntConstantNode(LexToken.DUMMY, i)

  protected fun argRef(i: Int) = ref(currentMethodScope.findLocalVariable(currentMethodScope.method.parameters[i].name)!!)
  protected fun lvRef(name: String) = ref(currentMethodScope.findLocalVariable(name)!!)

  protected fun notExpr(expr: ExpressionNode) = NotNode(expr)
  protected fun isEqualExpr(op1: ExpressionNode, op2: ExpressionNode) = IsEqualNode(op1, op2)
  protected fun isNotEqualExpr(op1: ExpressionNode, op2: ExpressionNode) = IsNotEqualNode(op1, op2)
  protected fun isInstanceExpr(type: JavaType, op2: ExpressionNode) = InstanceOfNode(type, op2, LexToken.DUMMY, LexToken.DUMMY)

  protected fun varAssignExpr(variable: Variable, expr: ExpressionNode, owner: ExpressionNode? = null): ExpressionNode {
    return VariableAssignmentNode(variable = variable, expression = caster.cast(variable.type, expr), owner = owner, tokenStart = LexToken.DUMMY, tokenEnd = LexToken.DUMMY)
  }

  protected fun plus(e1: ExpressionNode, e2: ExpressionNode): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return PlusNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  protected fun minus(e1: ExpressionNode, e2: ExpressionNode): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MinusNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  protected fun mul(e1: ExpressionNode, e2: ExpressionNode): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MulNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  protected inner class StatementsComposer(
    private val statements: MutableList<StatementNode>
  ) {

    fun addAllStmt(statements: List<StatementNode>) = this.statements.addAll(statements)
    fun addStmt(statement: StatementNode) = this.statements.add(statement)

    fun stmt(expr: ExpressionNode, add: Boolean = true): StatementNode {
      val statement = ExpressionStatementNode(expr)
      if (add) statements.add(statement)
      return statement
    }

    fun varAssignStmt(variable: Variable, expr: ExpressionNode, owner: ExpressionNode? = null): StatementNode {
      return stmt(varAssignExpr(variable, expr, owner))
    }

    fun returnStmt(expr: ExpressionNode? = null, add: Boolean = true): StatementNode {
      val statement =
        if (expr != null) ReturnStatementNode(caster.cast(currentMethodScope.method.returnType, expr))
        else ReturnStatementNode(null, LexToken.DUMMY, LexToken.DUMMY)
      if (add) statements.add(statement)
      return statement
    }

    fun ifStmt(condition: ExpressionNode, trueStmt: StatementNode, falseStmt: StatementNode? = null,
               add: Boolean = true): IfStatementNode {
      val statement = IfStatementNode(caster.truthyCast(condition), trueStmt, falseStmt, LexToken.DUMMY, LexToken.DUMMY)
      if (add) statements.add(statement)
      return statement
    }

    fun ifStmt(condition: ExpressionNode,
               add: Boolean = true, trueStatementsComposerFunc: StatementsComposer.() -> Unit): IfStatementNode {
      val trueStatementBlock = useInnerScope {
        val trueStatementsComposer = StatementsComposer(mutableListOf())
        trueStatementsComposerFunc.invoke(trueStatementsComposer)
        trueStatementsComposer.asBlockStatement()
      }
      val statement = IfStatementNode(caster.truthyCast(condition),
        trueStatementBlock, null,
        LexToken.DUMMY, LexToken.DUMMY)

      if (add) statements.add(statement)
      return statement
    }

    private fun asBlockStatement() = BlockStatementNode(statements, LexToken.DUMMY, LexToken.DUMMY)
  }

  protected fun newLambda(classNode: ClassNode,
                          parameters: List<MethodParameter>, returnType: JavaType, interfaceType: JavaType,
                          lambdaBodyStatementComposerFunc: StatementsComposer.() -> Unit): NewInstanceNode {

    val (lambdaClassNode, lambdaMethod, newInstanceNode) = createLambdaNode(
      outerClassNode = classNode,
      references = emptyList(),
      lambdaMethodParameters = parameters,
      returnType = returnType,
      interfaceType = interfaceType,
      tokenStart = LexToken.DUMMY,
      tokenEnd = LexToken.DUMMY
    )

    val statements = lambdaMethod.blockStatement.statements
    useScope(MethodScope(ClassScope(symbolResolver, lambdaClassNode.type, null, Scope.DEFAULT_IMPORTS), lambdaMethod)) {
      val statementComposer = StatementsComposer(statements)
      lambdaBodyStatementComposerFunc.invoke(statementComposer)
      if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
        statements.add(SemanticHelper.returnVoid(lambdaMethod))
      }
    }
    return newInstanceNode
  }
}