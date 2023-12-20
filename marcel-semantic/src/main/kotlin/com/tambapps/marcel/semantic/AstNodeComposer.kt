package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
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
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor

/**
 * Abstract class providing handy methods to constructor AST nodes in an elegant way
 */
abstract class AstNodeComposer: MarcelBaseSemantic() {

  protected fun parameter(type: JavaType, name: String) = MethodParameter(type, name)

  protected fun signature(
    ownerClass: JavaType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    isConstructor: Boolean = false
  ): JavaMethod {
    return JavaMethodImpl(ownerClass, visibility, name, parameters, returnType, isDefault, isAbstract, isStatic, isConstructor)
  }

  protected inline fun methodNode(
    ownerClass: JavaType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    statementsSupplier: StatementsComposer.() -> Unit
  ): MethodNode {
    val methodNode = MethodNode(name, parameters.toMutableList(), visibility, returnType, isStatic, LexToken.DUMMY, LexToken.DUMMY, ownerClass)
    val statements = mutableListOf<StatementNode>()
    methodNode.blockStatement = BlockStatementNode(statements, LexToken.DUMMY, LexToken.DUMMY)

    useScope(MethodScope(ClassScope(typeResolver, ownerClass, null, emptyList()), methodNode)) {
      val statementComposer = StatementsComposer(statements)
      statementsSupplier.invoke(statementComposer) // it will directly add the statements on the method's statements
    }

    if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
      statements.add(SemanticHelper.returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun returnStatement(expr: ExpressionNode, methodReturnType: JavaType): StatementNode {
    return ReturnStatementNode(
      caster.cast(methodReturnType, expr)
    )
  }

  protected fun ref(field: MarcelField): ReferenceNode {
    return ReferenceNode(
      owner = if (field.isMarcelStatic) null else ThisReferenceNode(field.owner, LexToken.DUMMY),
      variable = field,
      token = LexToken.DUMMY
    )
  }

  protected fun string(value: String) = StringConstantNode(value, LexToken.DUMMY, LexToken.DUMMY)
  protected fun string(parts: List<ExpressionNode>) = StringNode(parts, LexToken.DUMMY, LexToken.DUMMY)

  protected fun fCall(name: String, arguments: List<ExpressionNode>,
                      owner: ExpressionNode,
                      castType: JavaType? = null): ExpressionNode {
    val method = typeResolver.findMethodOrThrow(owner.type, name, arguments, LexToken.DUMMY)
    return fCall(LexToken.DUMMY, LexToken.DUMMY, method, arguments, owner, castType)
  }
  protected fun fCall(name: String, arguments: List<ExpressionNode>,
                      ownerType: JavaType,
                      castType: JavaType? = null): ExpressionNode {
    val method = typeResolver.findMethodOrThrow(ownerType, name, arguments, LexToken.DUMMY)
    return fCall(LexToken.DUMMY, LexToken.DUMMY, method, arguments, null, castType)
  }

  protected fun thisRef(type: JavaType) = ThisReferenceNode(type, LexToken.DUMMY)

  protected fun bool(b: Boolean) = BoolConstantNode(LexToken.DUMMY, b)
  protected fun int(i: Int) = IntConstantNode(LexToken.DUMMY, i)

  protected inner class StatementsComposer(
    private val statements: MutableList<StatementNode>
  ) {

    fun stmt(expr: ExpressionNode, add: Boolean = true): StatementNode {
      val statement = ExpressionStatementNode(expr)
      if (add) statements.add(statement)
      return statement
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

}