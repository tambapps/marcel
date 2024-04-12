package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ArrayMapFilterCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode

/**
 * CST node visitor to iterate over all subsequent nodes of a Concrete Syntax Tree
 *
 * @property consume
 * @constructor Create empty For each node visitor
 */
class ForEachNodeVisitor(
  val consume: (CstNode) -> Unit,
): ExpressionCstNodeVisitor<Unit, Unit>, StatementCstNodeVisitor<Unit> {
  override fun visit(node: BoolCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: DoubleCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: FloatCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IntCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: LongCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: NullCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: StringCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: RegexCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: CharCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: TemplateStringCstNode, smartCastType: Unit?) {
    consume(node)
    node.expressions.forEach { it.accept(this) }
  }

  override fun visit(node: MapCstNode, smartCastType: Unit?) {
    consume(node)
    node.entries.forEach { it.first.accept(this); it.second.accept(this) }
  }

  override fun visit(node: ArrayCstNode, smartCastType: Unit?) {
    consume(node)
    node.elements.forEach { it.accept(this) }
  }

  override fun visit(node: ArrayMapFilterCstNode, smartCastType: Unit?) {
    consume(node)
    node.inExpr.accept(this)
    node.mapExpr?.accept(this)
    node.filterExpr?.accept(this)
  }

  override fun visit(node: UnaryMinusCstNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: NotCstNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: BinaryOperatorCstNode, smartCastType: Unit?) {
    consume(node)
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
  }

  override fun visit(node: BinaryTypeOperatorCstNode, smartCastType: Unit?) {
    consume(node)
    node.leftOperand.accept(this)
  }

  override fun visit(node: ElvisThrowCstNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
    node.throwableException.accept(this)
  }
  override fun visit(node: TernaryCstNode, smartCastType: Unit?) {
    consume(node)
    node.testExpressionNode.accept(this)
    node.trueExpressionNode.accept(this)
    node.falseExpressionNode.accept(this)
  }

  override fun visit(node: ClassReferenceCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: ThisReferenceCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: SuperReferenceCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: DirectFieldReferenceCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IncrCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: IndexAccessCstNode, smartCastType: Unit?) {
    consume(node)
    node.ownerNode.accept(this)
    node.indexNodes.forEach { it.accept(this) }
  }

  override fun visit(node: ReferenceCstNode, smartCastType: Unit?) {
    consume(node)
  }

  override fun visit(node: FunctionCallCstNode, smartCastType: Unit?) {
    consume(node)
    node.namedArgumentNodes.forEach { it.second.accept(this) }
    node.positionalArgumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: SuperConstructorCallCstNode, smartCastType: Unit?) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: ThisConstructorCallCstNode, smartCastType: Unit?) {
    consume(node)
    node.arguments.forEach { it.accept(this) }
  }

  override fun visit(node: NewInstanceCstNode, smartCastType: Unit?) {
    consume(node)
    node.namedArgumentNodes.forEach { it.second.accept(this) }
    node.positionalArgumentNodes.forEach { it.accept(this) }
  }

  override fun visit(node: WhenCstNode, smartCastType: Unit?) {
    consume(node)
    node.branches.forEach { it.first.accept(this); it.second.accept(this) }
    node.elseStatement?.accept(this)
  }

  override fun visit(node: SwitchCstNode, smartCastType: Unit?) {
    consume(node)
    node.switchExpression.accept(this)
    node.branches.forEach { it.first.accept(this); it.second.accept(this) }
    node.elseStatement?.accept(this)
  }

  override fun visit(node: LambdaCstNode, smartCastType: Unit?) {
    consume(node)
    node.blockCstNode.accept(this)
  }

  override fun visit(node: AsyncBlockCstNode, smartCastType: Unit?) {
    consume(node)
    node.block.accept(this)
  }

  override fun visit(node: TruthyVariableDeclarationCstNode, smartCastType: Unit?) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: ExpressionStatementCstNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: ReturnCstNode) {
    consume(node)
    node.expressionNode?.accept(this)
  }

  override fun visit(node: VariableDeclarationCstNode) {
    consume(node)
    node.expressionNode?.accept(this)
  }

  override fun visit(node: MultiVarDeclarationCstNode) {
    consume(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: IfStatementCstNode) {
    consume(node)
    node.condition.accept(this)
    node.trueStatementNode.accept(this)
    node.falseStatementNode?.accept(this)
  }

  override fun visit(node: ForInCstNode) {
    consume(node)
    node.inNode.accept(this)
    node.statementNode.accept(this)
  }

  override fun visit(node: ForInMultiVarCstNode) {
    consume(node)
    node.inNode.accept(this)
    node.statementNode.accept(this)
  }
  override fun visit(node: ForVarCstNode) {
    consume(node)
    node.bodyStatement.accept(this)
    node.condition.accept(this)
    node.iteratorStatement.accept(this)
  }

  override fun visit(node: WhileCstNode) {
    consume(node)
    node.condition.accept(this)
    node.statement.accept(this)
  }

  override fun visit(node: DoWhileStatementCstNode) {
    consume(node)
    node.condition.accept(this)
    node.statement.accept(this)
  }

  override fun visit(node: BlockCstNode) {
    consume(node)
    node.statements.forEach { it.accept(this) }
  }

  override fun visit(node: BreakCstNode) {
    consume(node)
  }

  override fun visit(node: ContinueCstNode) {
    consume(node)
  }

  override fun visit(node: ThrowCstNode) {
    consume(node)
    node.expression.accept(this)
  }

  override fun visit(node: TryCatchCstNode) {
    consume(node)
    node.tryNode.accept(this)
    node.resources.forEach { it.accept(this) }
    node.catchNodes.forEach { it.third.accept(this) }
    node.finallyNode?.accept(this)
  }
}