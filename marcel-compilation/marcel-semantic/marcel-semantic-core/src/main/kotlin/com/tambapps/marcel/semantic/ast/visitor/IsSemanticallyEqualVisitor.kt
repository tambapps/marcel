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
import com.tambapps.marcel.semantic.ast.notEq
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

class IsSemanticallyEqualVisitor(
  private val node: AstNode
): ExpressionNodeVisitor<Boolean>, StatementNodeVisitor<Boolean> {
  override fun visit(node: NotNode) = eqTo<NotNode> { it.expressionNode eq node.expressionNode }

  override fun visit(node: IncrNode) = eqTo<IncrNode> {
    node.variable == it.variable && node.owner eq it.owner && node.amount == it.amount
  }

  override fun visit(node: DupNode) = eqTo<DupNode> { it.expression eq node.expression }

  override fun visit(node: YieldExpression) = eqTo<YieldExpression> {
    it.statement eq node.statement && it.expression eq node.expression
  }

  override fun visit(node: VariableAssignmentNode) = eqTo<VariableAssignmentNode> {
    it.variable == node.variable && it.expression eq node.expression && it.owner eq node.owner
  }

  override fun visit(node: ArrayIndexAssignmentNode) = eqTo<ArrayIndexAssignmentNode> {
    it.owner eq node.owner && it.indexExpr eq node.indexExpr && it.expression eq node.expression
  }

  private fun eq(node: BinaryOperatorNode) = eqTo<BinaryOperatorNode> {
    it.javaClass == node.javaClass &&
    it.leftOperand eq node.leftOperand
        && it.rightOperand eq node.rightOperand

  }

  override fun visit(node: LeftShiftNode) = eq(node)

  override fun visit(node: RightShiftNode) = eq(node)

  override fun visit(node: DivNode) = eq(node)

  override fun visit(node: MinusNode) = eq(node)

  override fun visit(node: ModNode) = eq(node)

  override fun visit(node: MulNode) = eq(node)

  override fun visit(node: PlusNode) = eq(node)

  override fun visit(node: IsEqualNode) = eq(node)

  override fun visit(node: IsNotEqualNode) = eq(node)

  override fun visit(node: AndNode) = eq(node)

  override fun visit(node: OrNode) = eq(node)

  override fun visit(node: GeNode) = eq(node)

  override fun visit(node: GtNode) = eq(node)

  override fun visit(node: LeNode) = eq(node)

  override fun visit(node: LtNode) = eq(node)

  override fun visit(node: ElvisNode) = eq(node)

  override fun visit(node: TernaryNode) = eqTo<TernaryNode> {
    it.testExpressionNode eq node.testExpressionNode
        && it.trueExpressionNode eq node.trueExpressionNode
        && it.falseExpressionNode eq node.falseExpressionNode
  }

  override fun visit(node: ConditionalExpressionNode) = eqTo<ConditionalExpressionNode> {
    it.condition eq node.condition
        && it.trueExpression eq node.trueExpression
        && it.falseExpression eq node.falseExpression
  }

  override fun visit(node: ArrayAccessNode) = eqTo<ArrayAccessNode> {
    it.owner eq node.owner && it.indexNode eq node.indexNode
  }

  override fun visit(node: FunctionCallNode) = eqTo<FunctionCallNode> {
    it.javaMethod == node.javaMethod && it.arguments eq node.arguments && it.owner eq node.owner
  }

  override fun visit(node: NewInstanceNode) = eqTo<NewInstanceNode> {
    it.javaMethod == node.javaMethod && it.arguments eq node.arguments
  }

  override fun visit(node: ThisConstructorCallNode) = eqTo<ThisConstructorCallNode> {
    it.classType == node.classType && it.arguments eq node.arguments && it.javaMethod == node.javaMethod
  }

  override fun visit(node: SuperConstructorCallNode)= eqTo<SuperConstructorCallNode> {
    it.classType == node.classType && it.arguments eq node.arguments && it.javaMethod == node.javaMethod
  }

  override fun visit(node: ReferenceNode) = eqTo<ReferenceNode> {
    it.owner eq node.owner && it.variable == node.variable
  }

  override fun visit(node: ClassReferenceNode) = eqTo<ClassReferenceNode> { it.classType == node.classType }

  override fun visit(node: ThisReferenceNode) = eqTo<ThisReferenceNode> { it.type == node.type }

  override fun visit(node: SuperReferenceNode) = eqTo<SuperReferenceNode> { it.type == node.type }

  override fun visit(node: JavaCastNode) = eqTo<JavaCastNode> {
    it.type == node.type && it.expressionNode eq node.expressionNode
  }

  override fun visit(node: InstanceOfNode) = eqTo<InstanceOfNode> {
    it.instanceType == node.instanceType && it.expressionNode eq node.expressionNode
  }

  override fun visit(node: BoolConstantNode) = eqTo<BoolConstantNode> { it.value == node.value }

  override fun visit(node: ByteConstantNode) = eqTo<ByteConstantNode> { it.value == node.value }

  override fun visit(node: CharConstantNode) = eqTo<CharConstantNode> { it.value == node.value }

  override fun visit(node: StringConstantNode) = eqTo<StringConstantNode> { it.value == node.value }

  override fun visit(node: StringNode) = eqTo<StringNode> {
    it.parts eq node.parts
  }

  override fun visit(node: DoubleConstantNode) = eqTo<DoubleConstantNode> { it.value == node.value }

  override fun visit(node: FloatConstantNode) = eqTo<FloatConstantNode> { it.value == node.value }

  override fun visit(node: IntConstantNode) = eqTo<IntConstantNode> { it.value == node.value }

  override fun visit(node: LongConstantNode) = eqTo<LongConstantNode> { it.value == node.value }

  override fun visit(node: NullValueNode) = this.node is NullValueNode

  override fun visit(node: ShortConstantNode) = eqTo<ShortConstantNode> { it.value == node.value }

  override fun visit(node: VoidExpressionNode) = eqTo<VoidExpressionNode> { true }

  override fun visit(node: ArrayNode) = eqTo<ArrayNode> {
    it.elements eq node.elements
  }

  override fun visit(node: NewArrayNode) = eqTo<NewArrayNode> {
    it.type == node.type && it.sizeExpr eq node.sizeExpr
  }


  override fun visit(node: MapNode) = eqTo<MapNode> {
    if (node.entries.size != it.entries.size) return@eqTo false
    for (i in node.entries.indices) {
      val (k1, v1) = node.entries[i]
      val (k2, v2) = it.entries[i]
      if (k1 notEq k2 || v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(node: ExprErrorNode) = false

  override fun visit(node: ExpressionStatementNode) = eqTo<ExpressionStatementNode> {
    it.expressionNode eq node.expressionNode
  }

  override fun visit(node: ReturnStatementNode) = eqTo<ReturnStatementNode> {
    it.expressionNode eq node.expressionNode
  }

  override fun visit(node: BlockStatementNode) = eqTo<BlockStatementNode> {
    it.statements eq node.statements
  }

  override fun visit(node: IfStatementNode) = eqTo<IfStatementNode> {
    node.conditionNode eq it.conditionNode
        && node.trueStatementNode eq it.trueStatementNode
        && node.falseStatementNode eq it.falseStatementNode
  }

  override fun visit(node: ForInIteratorStatementNode) = eqTo<ForInIteratorStatementNode> {
    it.variable == node.variable
        && it.iteratorVariable == node.iteratorVariable
        && it.iteratorExpression eq node.iteratorExpression
  }

  override fun visit(node: WhileNode) = eqTo<WhileNode> {
    it.condition eq node.condition && it.statement eq node.statement
  }

  override fun visit(node: DoWhileNode) = eqTo<DoWhileNode> {
    it.condition eq node.condition && it.statement eq node.statement
  }

  override fun visit(node: ForStatementNode) = eqTo<ForStatementNode> {
    it.initStatement notEq  node.initStatement
        && it.condition eq node.condition
        && it.iteratorStatement eq node.iteratorStatement
        && it.bodyStatement eq node.bodyStatement
  }

  override fun visit(node: BreakNode) = this.node is BreakNode

  override fun visit(node: ContinueNode) = this.node is BreakNode

  override fun visit(node: ThrowNode) = eqTo<ThrowNode> {
    it.expressionNode notEq  node.expressionNode
  }

  override fun visit(node: TryNode) = eqTo<TryNode> {
    if (it.tryStatementNode notEq  node.tryStatementNode) return@eqTo false
    if (it.catchNodes.size != node.catchNodes.size) return@eqTo false
    for (i in it.catchNodes.indices) {
      val catchNode = it.catchNodes[i]
      val otherCatchNode = node.catchNodes[i]

      if (catchNode.throwableTypes != otherCatchNode.throwableTypes) return@eqTo false
      if (catchNode.throwableVariable != otherCatchNode.throwableVariable) return@eqTo false
      if (catchNode.statement notEq otherCatchNode.statement) return@eqTo false
    }
    return@eqTo true
  }

  private inline fun <reified T> eqTo(compare: (T) -> Boolean): Boolean {
    if (node !is T) return false
    return compare(node)
  }

}