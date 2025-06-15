package com.tambapps.marcel.semantic.processor.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.processor.AbstractMarcelSemantic
import com.tambapps.marcel.semantic.processor.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
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
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.scope.Scope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.JavaArrayType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.MarcelField
import java.util.*

/**
 * Useful class allowing to compose easily statements of a method
 */
class StatementsComposer(
  scopeQueue: LinkedList<Scope>,
  override val caster: AstNodeCaster,
  override val symbolResolver: MarcelSymbolResolver,
  val statements: MutableList<StatementNode>,
  val tokenStart: LexToken,
  val tokenEnd: LexToken,
): AbstractMarcelSemantic(scopeQueue) {

  fun addAllStmt(statements: List<StatementNode>) = this.statements.addAll(statements)
  fun addStmt(statement: StatementNode) = this.statements.add(statement)

  fun superRef() = SuperReferenceNode(currentScope.classType.superType!!, tokenStart)

  fun ref(
    field: MarcelField,
    owner: ExpressionNode? = if (field.isMarcelStatic) null else ThisReferenceNode(
      field.owner,
      tokenStart
    )
  ): ReferenceNode {
    return ReferenceNode(
      owner = owner,
      variable = field,
      token = tokenStart
    )
  }

  // 0 is outer, 1 is outer of outer, and so on...
  fun outerRef(level: Int = 0) = ref(currentMethodScope.findField("this$$level")!!)

  fun cast(
    expr: ExpressionNode,
    type: JavaType
  ): ExpressionNode = caster.cast(type, expr)

  fun ref(methodParameter: MethodParameter) = ReferenceNode(
    owner = null,
    variable = currentMethodScope.findLocalVariable(methodParameter.name)!!, token = tokenStart
  )

  fun ref(lv: LocalVariable) = ReferenceNode(owner = null, variable = lv, token = tokenStart)

  fun string(value: String) = StringConstantNode(value, tokenStart, tokenEnd)
  fun string(parts: List<ExpressionNode>) =
    StringNode(parts, tokenStart, tokenEnd)

  fun array(
    asType: JavaArrayType,
    vararg elements: ExpressionNode
  ): ArrayNode {
    return array(asType, elements.toMutableList())
  }

  fun array(
    arrayType: JavaArrayType,
    elements: List<ExpressionNode>
  ): ArrayNode {
    return ArrayNode(
      if (elements is MutableList) elements else elements.toMutableList(),
      tokenStart, tokenEnd, arrayType
    )
  }

  fun fCall(
    name: String, arguments: List<ExpressionNode>,
    owner: ExpressionNode,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(owner.type, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  fun fCall(
    method: MarcelMethod, arguments: List<ExpressionNode>,
    owner: ExpressionNode,
    castType: JavaType? = null
  ): ExpressionNode {
    return fCall(tokenStart, tokenEnd, method, arguments, owner, castType)
  }

  fun fCall(
    name: String, arguments: List<ExpressionNode>,
    ownerType: JavaType,
    castType: JavaType? = null
  ): ExpressionNode {
    val method = symbolResolver.findMethodOrThrow(ownerType, name, arguments, tokenStart)
    return fCall(tokenStart, tokenEnd, method, arguments, null, castType)
  }

  fun superConstructorCall(method: MarcelMethod, arguments: List<ExpressionNode>) = SuperConstructorCallNode(
    method.ownerClass,
    method,
    arguments,
    tokenStart,
    tokenEnd
  )

  fun constructorCall(
    method: MarcelMethod,
    arguments: List<ExpressionNode>
  ): NewInstanceNode {
    return NewInstanceNode(method.ownerClass, method, castedArguments(method, arguments), tokenStart)
  }

  fun thisRef() = ThisReferenceNode(currentScope.classType, tokenStart)

  fun bool(b: Boolean) = BoolConstantNode(tokenStart, b)
  fun int(i: Int) = IntConstantNode(tokenStart, i)

  fun argRef(i: Int) =
    ref(currentMethodScope.findLocalVariable(currentMethodScope.method.parameters[i].name)!!)

  fun lvRef(name: String) = ref(currentMethodScope.findLocalVariable(name)!!)

  fun notExpr(expr: ExpressionNode) = NotNode(expr)
  fun isEqualExpr(
    op1: ExpressionNode,
    op2: ExpressionNode
  ) = IsEqualNode(op1, op2)

  fun isNotEqualExpr(
    op1: ExpressionNode,
    op2: ExpressionNode
  ) = IsNotEqualNode(op1, op2)

  fun isInstanceExpr(type: JavaType, op2: ExpressionNode) =
    InstanceOfNode(type, op2, tokenStart, tokenEnd)

  fun varAssignExpr(
    variable: Variable,
    expr: ExpressionNode,
    owner: ExpressionNode? = null
  ): ExpressionNode {
    return VariableAssignmentNode(
      variable = variable,
      expression = caster.cast(variable.type, expr),
      owner = owner,
      tokenStart = tokenStart,
      tokenEnd = tokenEnd
    )
  }

  fun plus(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return PlusNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  fun minus(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MinusNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  fun mul(
    e1: ExpressionNode,
    e2: ExpressionNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(e1, e2)
    return MulNode(caster.cast(commonType, e1), caster.cast(commonType, e2))
  }

  fun stmt(statement: StatementNode) {
    statements.add(statement)
  }

  fun stmt(expr: ExpressionNode) {
    val statement = ExpressionStatementNode(expr)
    statements.add(statement)
  }

  fun varAssignStmt(
    variable: Variable,
    expr: ExpressionNode,
    owner: ExpressionNode? = null
  ) = stmt(varAssignExpr(variable, expr, owner))

  fun returnVoidStmt() = returnStmt(VoidExpressionNode(tokenStart, tokenEnd))

  fun returnStmt(
    expr: ExpressionNode? = null,
  ) {
    val statement =
      if (expr != null) ReturnStatementNode(caster.cast(currentMethodScope.method.returnType, expr))
      else ReturnStatementNode(null, tokenStart, tokenEnd)
    statements.add(statement)
  }

  fun ifStmt(
    condition: ExpressionNode,
    trueStmt: StatementNode,
    falseStmt: StatementNode? = null,
    add: Boolean = true
  ): IfStatementNode {
    val statement = IfStatementNode(caster.truthyCast(condition), trueStmt, falseStmt, tokenStart, tokenEnd)
    if (add) statements.add(statement)
    return statement
  }

  inline fun ifStmt(
    condition: ExpressionNode, trueStatementsComposerFunc: StatementsComposer.() -> Unit
  ): IfStatementNode {
    val trueStatementBlock = useInnerScope {
      val trueStatementsComposer = StatementsComposer(scopeQueue, caster, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
      trueStatementsComposerFunc.invoke(trueStatementsComposer)
      trueStatementsComposer.asBlockStatement()
    }
    val statement = IfStatementNode(
      caster.truthyCast(condition),
      trueStatementBlock, null,
      tokenStart, tokenEnd
    )

    statements.add(statement)
    return statement
  }

  inline fun forInIteratorNodeStmt(forVariable: LocalVariable, inNode: ExpressionNode,
                                   forStatementsComposerFunc: StatementsComposer.(MethodScope, LocalVariable) -> Unit
  ) {
    useInnerScope { forScope ->
      val forStmt = forInIteratorNode(tokenStart, tokenEnd, forScope, forVariable, inNode) {
        val forStatementsComposer = StatementsComposer(scopeQueue, caster, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
        forStatementsComposerFunc.invoke(forStatementsComposer, forScope, forVariable)
        forStatementsComposer.asBlockStatement()
      }
      statements.add(forStmt)
    }
  }
  inline fun forInArrayStmt(array: ExpressionNode, forStatementsComposerFunc: StatementsComposer.(MethodScope, LocalVariable) -> Unit) {
    useInnerScope { forScope ->
      val iVar = forScope.addLocalVariable(JavaType.int, token = tokenStart)
      val forVariable = forScope.addLocalVariable(array.type.asArrayType.elementsType, token = tokenStart)
      val forStmt = forInArrayNode(tokenStart, tokenEnd, forScope = forScope, inNode = array, iVar = iVar, forVariable = forVariable) {
        val forStatementsComposer = StatementsComposer(scopeQueue, caster, symbolResolver, mutableListOf(), tokenStart, tokenEnd)
        forStatementsComposerFunc.invoke(forStatementsComposer, forScope, forVariable)
        forStatementsComposer.asBlockStatement()
      }
      statements.add(forStmt)
    }
  }
  fun throwStmt(expr: ExpressionNode) = stmt(ThrowNode(tokenStart, tokenEnd, expr))

  fun asBlockStatement() = BlockStatementNode(statements, tokenStart, tokenEnd)
}
