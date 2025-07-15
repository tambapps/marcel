package com.tambapps.marcel.semantic.ast.visitor

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.eq
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ConditionalExpressionNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExprErrorNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.YieldExpression
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
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode

class IsEqualVisitor(
  private val node: AstNode
): ExpressionNodeVisitor<Boolean>, StatementNodeVisitor<Boolean> {
  override fun visit(node: NotNode) = eqTo<NotNode> { it.expressionNode eq node.expressionNode }

  override fun visit(node: IncrNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: DupNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: YieldExpression): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: VariableAssignmentNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayIndexAssignmentNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: LeftShiftNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: RightShiftNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: DivNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: MinusNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ModNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: MulNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: PlusNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: IsEqualNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: IsNotEqualNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: AndNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: OrNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: GeNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: GtNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: LeNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: LtNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ElvisNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: TernaryNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ConditionalExpressionNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayAccessNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: FunctionCallNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: NewInstanceNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ThisConstructorCallNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: SuperConstructorCallNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ReferenceNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ClassReferenceNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ThisReferenceNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: SuperReferenceNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: JavaCastNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: InstanceOfNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: BoolConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ByteConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: CharConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: StringConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: StringNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: DoubleConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: FloatConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: IntConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: LongConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: NullValueNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ShortConstantNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: VoidExpressionNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: NewArrayNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: MapNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ExprErrorNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ExpressionStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ReturnStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: BlockStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: IfStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ForInIteratorStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: WhileNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: DoWhileNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ForStatementNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: BreakNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ContinueNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: ThrowNode): Boolean {
    TODO("Not yet implemented")
  }

  override fun visit(node: TryNode): Boolean {
    TODO("Not yet implemented")
  }

  private inline fun <reified T> eqTo(compare: (T) -> Boolean): Boolean {
    if (node !is T) return false
    return compare(node)
  }

}