package com.tambapps.marcel.parser.ast.visitor

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

class CheckAllPathsReturnVisitor: AstNodeVisitor<Boolean> {
  override fun visit(node: IntConstantNode) = false

  override fun visit(node: LongConstantNode) = false

  override fun visit(node: FloatConstantNode) = false

  override fun visit(node: DoubleConstantNode) = false

  override fun visit(node: CharConstantNode) = false

  override fun visit(node: MulOperator) = false

  override fun visit(node: TernaryNode) = false

  override fun visit(node: ElvisOperator) = false

  override fun visit(node: FunctionCallNode) = false
  override fun visit(node: MethodDefaultParameterMethodCall) = false

  override fun visit(node: ConstructorCallNode) = false

  override fun visit(node: NamedParametersConstructorCallNode) = false
  override fun visit(node: SuperConstructorCallNode) = false
  override fun visit(node: ThisConstructorCallNode) = false

  override fun visit(node: DivOperator) = false

  override fun visit(node: PlusOperator) = false

  override fun visit(node: MinusOperator) = false

  override fun visit(node: PowOperator) = false

  override fun visit(node: RightShiftOperator) = false

  override fun visit(node: LeftShiftOperator) = false

  override fun visit(node: VariableAssignmentNode) = false

  override fun visit(node: FieldAssignmentNode) = false

  override fun visit(node: IndexedVariableAssignmentNode) = false

  override fun visit(node: ReferenceExpression) = false

  override fun visit(node: IndexedReferenceExpression) = false

  override fun visit(node: UnaryMinus) = false

  override fun visit(node: UnaryPlus) = false

  override fun visit(node: BlockNode): Boolean {
    return if (node.statements.isNotEmpty()) node.statements.last().accept(this)
    else false
  }

  override fun visit(node: FunctionBlockNode): Boolean {
    return if (node.statements.isNotEmpty()) node.statements.last().accept(this)
    else false
  }

  override fun visit(node: LambdaNode) = false

  override fun visit(node: ExpressionStatementNode): Boolean {
    return node is ReturnNode
  }

  override fun visit(node: VariableDeclarationNode) = false

  override fun visit(node: TruthyVariableDeclarationNode) = false

  override fun visit(node: MultiVariableDeclarationNode) = false

  override fun visit(node: ReturnNode): Boolean {
    return true
  }

  override fun visit(node: VoidExpression) = false

  override fun visit(node: StringNode) = false

  override fun visit(node: StringConstantNode) = false

  override fun visit(node: AsNode) = false

  override fun visit(node: ToStringNode) = false

  override fun visit(node: InvokeAccessOperator) = false

  override fun visit(node: GetFieldAccessOperator) = false
  override fun visit(node: GetIndexFieldAccessOperator) = false

  override fun visit(node: BooleanConstantNode) = false

  override fun visit(node: ComparisonOperatorNode) = false

  override fun visit(node: AndOperator) = false

  override fun visit(node: OrOperator) = false

  override fun visit(node: NotNode) = false

  override fun visit(node: IfStatementNode): Boolean {
    if (node.falseStatementNode == null) return false
    return node.trueStatementNode.accept(this) && node.falseStatementNode!!.accept(this)
  }

  override fun visit(node: ForStatement) = false

  override fun visit(node: TryCatchNode): Boolean {
    return node.tryStatementNode.accept(this) && (
        node.catchNodes.isEmpty() || node.catchNodes.all { it.statementNode.accept(this) }
        )
  }
  override fun visit(node: ForInStatement) = false

  override fun visit(node: WhileStatement) = false

  override fun visit(node: BooleanExpressionNode) = false

  override fun visit(node: NullValueNode) = false

  override fun visit(node: IncrNode) = false

  override fun visit(node: BreakLoopNode) = false

  override fun visit(node: ContinueLoopNode) = false

  override fun visit(node: RangeNode) = false

  override fun visit(node: LiteralArrayNode) = false

  override fun visit(node: LiteralMapNode) = false

  override fun visit(node: SwitchBranchNode): Boolean {
    return node.statementNode.accept(this)
  }

  override fun visit(node: SwitchNode): Boolean {
    return node.elseStatement != null && node.branches.all { it.accept(this) }
  }

  override fun visit(node: WhenBranchNode): Boolean {
    return node.statementNode.accept(this)
  }

  override fun visit(node: WhenNode): Boolean {
    return node.elseStatement != null && node.branches.all { it.accept(this) }
  }

  override fun visit(node: IsOperator) = false
  override fun visit(node: IsNotOperator) = false

  override fun visit(node: ShortConstantNode) = false

  override fun visit(node: ByteConstantNode) = false

  override fun visit(node: SuperReference) = false

  override fun visit(node: ThisReference) = false
  override fun visit(node: LiteralPatternNode) = false

  override fun visit(node: FindOperator) = false

  override fun visit(node: ClassExpressionNode) = false
  override fun visit(node: DirectFieldAccessNode) = false

}