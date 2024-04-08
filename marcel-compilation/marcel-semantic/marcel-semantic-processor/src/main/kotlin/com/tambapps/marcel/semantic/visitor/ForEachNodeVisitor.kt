package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.PopNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NewArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.OrNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode

class ForEachNodeVisitor(
  val consume: (AstNode) -> Unit
) : StatementNodeVisitor<Unit>, ExpressionNodeVisitor<Unit> {
  override fun visit(node: NotNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: IncrNode) {
    consume(node)
    node.owner?.accept(this)
  }

  override fun visit(node: DupNode) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: PopNode) {
    consume(node)
  }

  override fun visit(node: VariableAssignmentNode) {
    consume(node)
    node.owner?.accept(this)
    node.expression.accept(this)
  }

  override fun visit(node: ArrayIndexAssignmentNode) {
    consume(node)
    node.owner.accept(this)
    node.expression.accept(this)
  }

  override fun visit(node: LeftShiftNode) = binaryOperator(node)

  override fun visit(node: RightShiftNode) = binaryOperator(node)

  override fun visit(node: DivNode) = binaryOperator(node)

  override fun visit(node: MinusNode) = binaryOperator(node)

  override fun visit(node: ModNode) = binaryOperator(node)

  override fun visit(node: MulNode) = binaryOperator(node)

  override fun visit(node: PlusNode) = binaryOperator(node)

  override fun visit(node: IsEqualNode) = binaryOperator(node)

  override fun visit(node: IsNotEqualNode) = binaryOperator(node)

  override fun visit(node: AndNode) = binaryOperator(node)

  override fun visit(node: OrNode) = binaryOperator(node)

  override fun visit(node: GeNode) = binaryOperator(node)

  override fun visit(node: GtNode) = binaryOperator(node)

  override fun visit(node: LeNode) = binaryOperator(node)

  override fun visit(node: LtNode) = binaryOperator(node)

  override fun visit(node: ElvisNode) = binaryOperator(node)

  override fun visit(node: TernaryNode) {
    consume(node)
    node.testExpressionNode.accept(this)
    node.trueExpressionNode.accept(this)
    node.falseExpressionNode.accept(this)
  }

  override fun visit(node: ArrayAccessNode) {
    consume(node)
    node.owner.accept(this)
    node.indexNode.accept(this)
  }

  override fun visit(node: FunctionCallNode) {
    consume(node)
    node.owner?.accept(this)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: NewInstanceNode) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: ThisConstructorCallNode) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: SuperConstructorCallNode) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: ReferenceNode) {
    consume(node)
  }

  override fun visit(node: ClassReferenceNode) {
    consume(node)
  }

  override fun visit(node: ThisReferenceNode) {
    consume(node)
  }

  override fun visit(node: SuperReferenceNode) {
    consume(node)
  }

  override fun visit(node: JavaCastNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: InstanceOfNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: BoolConstantNode) {
    consume(node)
  }

  override fun visit(node: ByteConstantNode) {
    consume(node)
  }

  override fun visit(node: CharConstantNode) {
    consume(node)
  }

  override fun visit(node: StringConstantNode) {
    consume(node)
  }

  override fun visit(node: StringNode) {
    consume(node)
    node.parts.forEach { it.accept(this) }

  }

  override fun visit(node: DoubleConstantNode) {
    consume(node)
  }

  override fun visit(node: FloatConstantNode) {
    consume(node)
  }

  override fun visit(node: IntConstantNode) {
    consume(node)
  }

  override fun visit(node: LongConstantNode) {
    consume(node)
  }

  override fun visit(node: NullValueNode) {
    consume(node)
  }

  override fun visit(node: ShortConstantNode) {
    consume(node)
  }

  override fun visit(node: VoidExpressionNode) {
    consume(node)
  }

  override fun visit(node: ArrayNode) {
    consume(node)
    node.elements.forEach { it.accept(this) }
  }

  override fun visit(node: NewArrayNode) {
    consume(node)
    node.sizeExpr.accept(this)
  }

  override fun visit(node: MapNode) {
    consume(node)
    node.entries.forEach { it.first.accept(this); it.second.accept(this) }
  }

  override fun visit(node: ExpressionStatementNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: ReturnStatementNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: BlockStatementNode) {
    consume(node)
    node.statements.forEach { it.accept(this) }
  }

  override fun visit(node: IfStatementNode) {
    consume(node)
    node.conditionNode.accept(this)
    node.trueStatementNode.accept(this)
    node.falseStatementNode?.accept(this)
  }

  override fun visit(node: ForInIteratorStatementNode) {
    consume(node)
    node.iteratorExpression.accept(this)
    node.bodyStatement.accept(this)
    node.nextMethodCall.accept(this)
  }

  override fun visit(node: WhileNode) {
    consume(node)
    node.condition.accept(this)
    node.statement.accept(this)
  }

  override fun visit(node: DoWhileNode) {
    consume(node)
    node.condition.accept(this)
    node.statement.accept(this)
  }

  override fun visit(node: ForStatementNode) {
    consume(node)
    node.initStatement.accept(this)
    node.condition.accept(this)
    node.iteratorStatement.accept(this)
    node.bodyStatement.accept(this)
  }

  override fun visit(node: BreakNode) = consume(node)

  override fun visit(node: ContinueNode) = consume(node)

  override fun visit(node: ThrowNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: TryNode) {
    consume(node)
    node.tryStatementNode.accept(this)
    node.catchNodes.forEach { it.statement.accept(this) }
    node.finallyNode?.statement?.accept(this)
  }

  private fun binaryOperator(node: BinaryOperatorNode) {
    consume(node)
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
  }
}