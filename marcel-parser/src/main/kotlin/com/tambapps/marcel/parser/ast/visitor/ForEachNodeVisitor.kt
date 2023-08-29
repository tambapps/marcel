package com.tambapps.marcel.parser.ast.visitor

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.TryCatchNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

class ForEachNodeVisitor(private val consumer: (AstNode) -> Unit): AstNodeVisitor<Unit> {


  override fun visit(node: IntConstantNode) = consumer.invoke(node)

  override fun visit(node: LongConstantNode) = consumer.invoke(node)

  override fun visit(node: FloatConstantNode) = consumer.invoke(node)

  override fun visit(node: DoubleConstantNode) = consumer.invoke(node)

  override fun visit(node: CharConstantNode) = consumer.invoke(node)
  override fun visit(node: ClassExpressionNode) = consumer.invoke(node)
  override fun visit(node: DirectFieldAccessNode) = consumer.invoke(node)

  override fun visit(node: MulOperator) = visitBinaryOperator(node)
  override fun visit(node: FindOperator) = visitBinaryOperator(node)

  private fun visitBinaryOperator(operator: BinaryOperatorNode) {
    consumer.invoke(operator)
    operator.leftOperand.accept(this)
    operator.rightOperand.accept(this)
  }

  override fun visit(node: TernaryNode) {
    consumer.invoke(node)
    node.boolExpression.accept(this)
    node.trueExpression.accept(this)
    node.falseExpression.accept(this)
  }

  override fun visit(node: ElvisOperator) = visitBinaryOperator(node)

  override fun visit(node: FunctionCallNode) {
    consumer.invoke(node)
    if (node is NamedParametersFunctionCall) node.argumentNodes.forEach { it.accept(this) }
    else node.argumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: MethodDefaultParameterMethodCall) = consumer.invoke(node)
  override fun visit(node: ConstructorCallNode) {
    consumer.invoke(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: SuperConstructorCallNode) {
    consumer.invoke(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: ThisConstructorCallNode) {
    consumer.invoke(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: NamedParametersConstructorCallNode) {
    consumer.invoke(node)
    node.argumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: DivOperator) = visitBinaryOperator(node)

  override fun visit(node: PlusOperator) = visitBinaryOperator(node)

  override fun visit(node: MinusOperator) = visitBinaryOperator(node)

  override fun visit(node: PowOperator) = visitBinaryOperator(node)

  override fun visit(node: RightShiftOperator) = visitBinaryOperator(node)

  override fun visit(node: LeftShiftOperator) = visitBinaryOperator(node)

  override fun visit(node: VariableAssignmentNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: FieldAssignmentNode) {
    consumer.invoke(node)
    node.fieldNode.accept(this)
    node.expression.accept(this)
  }

  override fun visit(node: IndexedVariableAssignmentNode) {
    consumer.invoke(node)
    node.indexedReference.accept(this)
    node.expression.accept(this)
  }

  override fun visit(node: ReferenceExpression) = consumer.invoke(node)

  override fun visit(node: IndexedReferenceExpression) {
    consumer.invoke(node)
    node.indexArguments.forEach { it.accept(this) }
  }

  override fun visit(node: UnaryMinus) = visitUnaryOperator(node)

  private fun visitUnaryOperator(unaryOperator: UnaryOperator) {
    consumer.invoke(unaryOperator)
    unaryOperator.operand.accept(this)
  }

  override fun visit(node: UnaryPlus) = visitUnaryOperator(node)

  override fun visit(node: BlockNode) {
    consumer.invoke(node)
    node.statements.forEach { it.accept(this) }
  }

  override fun visit(node: FunctionBlockNode) {
    consumer.invoke(node)
    node.statements.forEach { it.accept(this) }
  }

  override fun visit(node: LambdaNode) {
    consumer.invoke(node)
    node.blockNode.accept(this)
  }

  override fun visit(node: ExpressionStatementNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: VariableDeclarationNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: TruthyVariableDeclarationNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: MultiVariableDeclarationNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: ReturnNode) {
    consumer.invoke(node)
    node.expression.accept(this)
  }

  override fun visit(node: VoidExpression) {
    consumer.invoke(node)
  }

  override fun visit(node: StringNode) {
    consumer.invoke(node)
    node.parts.forEach { it.accept(this) }
  }

  override fun visit(node: StringConstantNode) = consumer.invoke(node)

  override fun visit(node: AsNode) {
    consumer.invoke(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: ToStringNode) {
    consumer.invoke(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: InvokeAccessOperator) = visitBinaryOperator(node)

  override fun visit(node: GetFieldAccessOperator) = visitBinaryOperator(node)

  override fun visit(node: GetIndexFieldAccessOperator) = visitBinaryOperator(node)

  override fun visit(node: BooleanConstantNode) = consumer.invoke(node)

  override fun visit(node: ComparisonOperatorNode) = visitBinaryOperator(node)

  override fun visit(node: AndOperator) = visitBinaryOperator(node)

  override fun visit(node: OrOperator) = visitBinaryOperator(node)

  override fun visit(node: NotNode) = visitUnaryOperator(node)

  override fun visit(node: IfStatementNode) {
    consumer.invoke(node)
    node.trueStatementNode.accept(this)
    node.falseStatementNode?.accept(this)
  }

  override fun visit(node: ForStatement) {
    consumer.invoke(node)
    node.initStatement.accept(this)
    node.iteratorStatement.accept(this)
    node.endCondition.accept(this)
    node.body.accept(this)
  }

  override fun visit(node: TryCatchNode) {
    consumer.invoke(node)
    node.tryStatementNode.accept(this)
    node.catchNodes.forEach { it.statementNode.accept(this) }
    node.finallyBlock?.statementNode?.accept(this)
  }

  override fun visit(node: ForInStatement) {
    consumer.invoke(node)
    node.inExpression.accept(this)
    node.body.accept(this)
  }

  override fun visit(node: WhileStatement) {
    consumer.invoke(node)
    node.condition.accept(this)
    node.body.accept(this)
  }

  override fun visit(node: BooleanExpressionNode) {
    consumer.invoke(node)
    node.innerExpression.accept(this)
  }

  override fun visit(node: NullValueNode) = consumer.invoke(node)

  override fun visit(node: IncrNode) {
    consumer.invoke(node)
    node.variableReference.accept(this)
  }

  override fun visit(node: BreakLoopNode) = consumer.invoke(node)

  override fun visit(node: ContinueLoopNode) = consumer.invoke(node)

  override fun visit(node: RangeNode) {
    consumer.invoke(node)
    node.from.accept(this)
    node.to.accept(this)
  }

  override fun visit(node: LiteralArrayNode) {
    consumer.invoke(node)
    node.elements.forEach { it.accept(this) }
  }

  override fun visit(node: LiteralMapNode) {
    consumer.invoke(node)
    node.entries.forEach {
      it.first.accept(this)
      it.second.accept(this)
    }
  }

  override fun visit(node: SwitchBranchNode) {
    consumer.invoke(node)
    node.conditionExpressionNode.accept(this)
    node.statementNode.accept(this)
  }

  override fun visit(node: SwitchNode) {
    consumer.invoke(node)
    node.expressionNode.accept(this)
    node.branches.forEach { it.accept(this) }
  }

  override fun visit(node: WhenNode) {
    consumer.invoke(node)
    node.branches.forEach { it.accept(this) }
  }

  override fun visit(node: WhenBranchNode) {
    consumer.invoke(node)
    node.conditionExpressionNode.accept(this)
    node.statementNode.accept(this)
  }

  override fun visit(node: IsOperator) = visitBinaryOperator(node)
  override fun visit(node: IsNotOperator) = visitBinaryOperator(node)

  override fun visit(node: ShortConstantNode) = consumer.invoke(node)

  override fun visit(node: ByteConstantNode) = consumer.invoke(node)

  override fun visit(node: ThisReference) = consumer.invoke(node)

  override fun visit(node: SuperReference) = consumer.invoke(node)

  override fun visit(node: LiteralPatternNode) = consumer.invoke(node)
}