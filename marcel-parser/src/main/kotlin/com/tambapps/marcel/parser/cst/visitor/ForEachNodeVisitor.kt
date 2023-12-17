package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallNode
import com.tambapps.marcel.parser.cst.expression.LambdaNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceNode
import com.tambapps.marcel.parser.cst.expression.NotNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.cst.expression.SwitchNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringNode
import com.tambapps.marcel.parser.cst.expression.TernaryNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusNode
import com.tambapps.marcel.parser.cst.expression.WhenNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolNode
import com.tambapps.marcel.parser.cst.expression.literal.CharNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatNode
import com.tambapps.marcel.parser.cst.expression.literal.IntNode
import com.tambapps.marcel.parser.cst.expression.literal.LongNode
import com.tambapps.marcel.parser.cst.expression.literal.MapNode
import com.tambapps.marcel.parser.cst.expression.literal.NullNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexNode
import com.tambapps.marcel.parser.cst.expression.literal.StringNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceNode
import com.tambapps.marcel.parser.cst.statement.BlockNode
import com.tambapps.marcel.parser.cst.statement.BreakNode
import com.tambapps.marcel.parser.cst.statement.ContinueNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.cst.statement.ForInNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarNode
import com.tambapps.marcel.parser.cst.statement.ForVarNode
import com.tambapps.marcel.parser.cst.statement.IfStatementNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationNode
import com.tambapps.marcel.parser.cst.statement.ReturnNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.ThrowNode
import com.tambapps.marcel.parser.cst.statement.TryCatchNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.cst.statement.WhileNode

class ForEachNodeVisitor(
  val consume: (CstNode) -> Unit,
): ExpressionCstNodeVisitor<Unit, Unit>, StatementCstNodeVisitor<Unit> {
  override fun visit(node: BoolNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: DoubleNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: FloatNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IntNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: LongNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: NullNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: StringNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: RegexNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: CharNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: TemplateStringNode, smartCastType: Unit?) {
    consume(node)
    node.expressions.forEach { it.accept(this) }
  }

  override fun visit(node: MapNode, smartCastType: Unit?) {
    consume(node)
    node.entries.forEach { it.first.accept(this); it.second.accept(this) }
  }

  override fun visit(node: ArrayNode, smartCastType: Unit?) {
    consume(node)
    node.elements.forEach { it.accept(this) }
  }

  override fun visit(node: UnaryMinusNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: NotNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: BinaryOperatorNode, smartCastType: Unit?) {
    consume(node)
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
  }

  override fun visit(node: BinaryTypeOperatorNode, smartCastType: Unit?) {
    consume(node)
    node.leftOperand.accept(this)
  }

  override fun visit(node: TernaryNode, smartCastType: Unit?) {
    consume(node)
    node.testExpressionNode.accept(this)
    node.trueExpressionNode.accept(this)
    node.falseExpressionNode.accept(this)
  }

  override fun visit(node: ClassReferenceNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: ThisReferenceNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: SuperReferenceNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: DirectFieldReferenceNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IncrNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IndexAccessNode, smartCastType: Unit?) {
    consume(node)
    node.ownerNode.accept(this)
    node.indexNodes.forEach { it.accept(this) }
  }

  override fun visit(node: ReferenceNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: FunctionCallNode, smartCastType: Unit?) {
    consume(node)
    node.namedArgumentNodes.forEach { it.second.accept(this) }
    node.positionalArgumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: SuperConstructorCallNode, smartCastType: Unit?) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: ThisConstructorCallNode, smartCastType: Unit?) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: NewInstanceNode, smartCastType: Unit?) {
    consume(node)
    node.namedArgumentNodes.forEach { it.second.accept(this) }
    node.positionalArgumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: WhenNode, smartCastType: Unit?) {
    consume(node)
    node.branches.forEach { it.first.accept(this); it.second.accept(this) }
    node.elseStatement?.accept(this)
  }

  override fun visit(node: SwitchNode, smartCastType: Unit?) {
    consume(node)
    node.switchExpression.accept(this)
    node.branches.forEach { it.first.accept(this); it.second.accept(this) }
    node.elseStatement?.accept(this)
  }

  override fun visit(node: LambdaNode, smartCastType: Unit?) {
    consume(node)
    node.blockCstNode.accept(this)
  }

  override fun visit(node: TruthyVariableDeclarationNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: ExpressionStatementNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: ReturnNode) {
    consume(node)
    node.expressionNode?.accept(this)
  }

  override fun visit(node: VariableDeclarationNode) {
    consume(node)
    node.expressionNode?.accept(this)
  }

  override fun visit(node: MultiVarDeclarationNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: IfStatementNode) {
    consume(node)
    node.condition.accept(this)
    node.trueStatementNode.accept(this)
    node.falseStatementNode?.accept(this)
  }

  override fun visit(node: ForInNode) {
    consume(node)
    node.inNode.accept(this)
    node.statementNode.accept(this)
  }

  override fun visit(node: ForInMultiVarNode) {
    consume(node)
    node.inNode.accept(this)
    node.statementNode.accept(this)
  }
  override fun visit(node: ForVarNode) {
    consume(node)
    node.bodyStatement.accept(this)
    node.condition.accept(this)
    node.iteratorStatement.accept(this)
  }

  override fun visit(node: WhileNode) {
    consume(node)
    node.condition.accept(this)
    node.statement.accept(this)
  }

  override fun visit(node: BlockNode) {
    consume(node)
    node.statements.forEach { it.accept(this) }
  }

  override fun visit(node: BreakNode) {
    consume(node)
  }

  override fun visit(node: ContinueNode) {
    consume(node)
  }

  override fun visit(node: ThrowNode) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: TryCatchNode) {
    consume(node)
    node.tryNode.accept(this)
    node.resources.forEach { it.accept(this) }
    node.catchNodes.forEach { it.third.accept(this) }
    node.finallyNode?.accept(this)
  }
}