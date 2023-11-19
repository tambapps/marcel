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
import com.tambapps.marcel.parser.ast.statement.ThrowStatementNode
import com.tambapps.marcel.parser.ast.statement.TryCatchNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

class FindNodeVisitor(private val predicate: (AstNode) -> Boolean): AstNodeVisitor<AstNode?> {

  private fun node(astNode: AstNode): AstNode? {
    return if (predicate.invoke(astNode)) astNode else null
  }

  private fun binaryNode(operator: BinaryOperatorNode): AstNode? {
    return node(operator) ?: operator.leftOperand.accept(this) ?: operator.rightOperand.accept(this)
  }

  override fun visit(node: IntConstantNode) = node(node)

  override fun visit(node: LongConstantNode) = node(node)

  override fun visit(node: FloatConstantNode) = node(node)

  override fun visit(node: DoubleConstantNode) = node(node)

  override fun visit(node: CharConstantNode) = node(node)

  override fun visit(node: MulOperator) = binaryNode(node)
  override fun visit(node: ModOperator) = binaryNode(node)

  override fun visit(node: TernaryNode): AstNode? {
    return node(node) ?: node.boolExpression.accept(this)
    ?: node.trueExpression.accept(this) ?: node.falseExpression.accept(this)
  }

  override fun visit(node: ElvisOperator) = binaryNode(node)

  override fun visit(node: FunctionCallNode) = node(node) ?: node.argumentNodes.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: MethodDefaultParameterMethodCall) = node(node)
  override fun visit(node: ConstructorCallNode) = node(node) ?: node.arguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: NamedParametersConstructorCallNode) = node(node) ?: node.argumentNodes.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: SuperConstructorCallNode) = node(node) ?: node.arguments.firstNotNullOfOrNull { it.accept(this) }
  override fun visit(node: ThisConstructorCallNode) = node(node) ?: node.arguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: DivOperator) = binaryNode(node)

  override fun visit(node: PlusOperator) = binaryNode(node)

  override fun visit(node: MinusOperator) = binaryNode(node)

  override fun visit(node: PowOperator) = binaryNode(node)

  override fun visit(node: RightShiftOperator) = binaryNode(node)

  override fun visit(node: LeftShiftOperator) = binaryNode(node)

  override fun visit(node: VariableAssignmentNode) = node(node) ?: node.expression.accept(this)

  override fun visit(node: FieldAssignmentNode) = node(node) ?: node.fieldNode.accept(this)
  ?: node.expression.accept(this)

  override fun visit(node: IndexedVariableAssignmentNode) = node(node)
    ?: node.indexedReference.accept(this) ?: node.expression.accept(this)

  override fun visit(node: ReferenceExpression) = node(node)

  override fun visit(node: IndexedReferenceExpression) = node(node)
    ?: node.indexArguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: UnaryMinus) = node(node) ?: node.operand.accept(this)

  override fun visit(node: UnaryPlus) = node(node) ?: node.operand.accept(this)

  override fun visit(node: BlockNode) = node(node) ?: node.statements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: FunctionBlockNode) = node(node) ?: node.statements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: LambdaNode) = node(node) ?: node.blockNode.accept(this)

  override fun visit(node: ExpressionStatementNode) = node(node) ?: node.expression.accept(this)

  override fun visit(node: VariableDeclarationNode) = node(node) ?: node.expression.accept(this)

  override fun visit(node: TruthyVariableDeclarationNode) = node(node)
    ?: node.expression.accept(this)

  override fun visit(node: MultiVariableDeclarationNode) = node(node)
    ?: node.expression.accept(this)

  override fun visit(node: ReturnNode) = node(node) ?: node.expression.accept(this)

  override fun visit(node: VoidExpression) = node(node)

  override fun visit(node: StringNode) = node(node) ?: node.parts.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: StringConstantNode) = node(node)

  override fun visit(node: AsNode) = node(node) ?: node.expressionNode.accept(this)
  override fun visit(node: InstanceofNode) = node(node) ?: node.expressionNode.accept(this)

  override fun visit(node: ToStringNode) = node(node) ?: node.expressionNode.accept(this)

  override fun visit(node: InvokeAccessOperator) = binaryNode(node)

  override fun visit(node: GetFieldAccessOperator) = binaryNode(node)

  override fun visit(node: GetIndexFieldAccessOperator) = binaryNode(node)

  override fun visit(node: BooleanConstantNode) = node(node)

  override fun visit(node: ComparisonOperatorNode) = binaryNode(node)

  override fun visit(node: AndOperator) = binaryNode(node)

  override fun visit(node: OrOperator) = binaryNode(node)

  override fun visit(node: NotNode) = node(node) ?: node.operand.accept(this)
  override fun visit(node: ThrowStatementNode) = node(node) ?: node.throwableExpression.accept(this)

  override fun visit(node: IfStatementNode) = node(node) ?: node.condition.accept(this)
  ?: node.trueStatementNode.accept(this) ?: node.falseStatementNode?.accept(this)

  override fun visit(node: ForStatement) = node(node) ?: node.initStatement.accept(this)
  ?: node.iteratorStatement.accept(this) ?: node.endCondition.accept(this)

  override fun visit(node: TryCatchNode) = node.tryBlock.statementNode.accept(this)
    ?: node.catchNodes.firstNotNullOfOrNull { it.statementNode.accept(this) }
    ?: node.finallyBlock?.statementNode?.accept(this)

  override fun visit(node: ForInStatement) = node(node) ?: node.inExpression.accept(this)

  override fun visit(node: WhileStatement) = node(node) ?: node.condition.accept(this)
  ?: node.body.accept(this)

  override fun visit(node: BooleanExpressionNode) = node(node) ?: node.innerExpression.accept(this)

  override fun visit(node: NullValueNode) = node(node)

  override fun visit(node: IncrNode) = node(node) ?: node.variableReference.accept(this)

  override fun visit(node: BreakLoopNode) = node(node)

  override fun visit(node: ContinueLoopNode) = node(node)

  override fun visit(node: RangeNode) = node(node) ?: node.from.accept(this) ?: node.to.accept(this)

  override fun visit(node: LiteralArrayNode) = node(node) ?: node.elements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: LiteralMapNode) = node(node) ?:
  node.entries.firstNotNullOfOrNull { it.first.accept(this) ?: it.second.accept(this) }

  override fun visit(node: SwitchBranchNode) = node(node) ?: node.conditionExpressionNode.accept(this)
  ?: node.statementNode.accept(this)

  override fun visit(node: SwitchNode) = node(node) ?: node.expressionNode.accept(this)
  ?: node.elseStatement?.accept(this)
  ?: node.branches.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: WhenBranchNode)= node(node) ?: node.conditionExpressionNode.accept(this)
  ?: node.statementNode.accept(this)

  override fun visit(node: WhenNode) = node(node)
    ?: node.elseStatement?.accept(this)
    ?: node.branches.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(node: IsOperator) = binaryNode(node)
  override fun visit(node: IsNotOperator) = binaryNode(node)

  override fun visit(node: ByteConstantNode) = node(node)

  override fun visit(node: ShortConstantNode) = node(node)

  override fun visit(node: ThisReference) = node(node)

  override fun visit(node: SuperReference) = node(node)

  override fun visit(node: LiteralPatternNode) = node(node)
  override fun visit(node: DirectFieldAccessNode) = node(node)

  override fun visit(node: ClassExpressionNode) = node(node)

  override fun visit(node: FindOperator) = binaryNode(node)
}