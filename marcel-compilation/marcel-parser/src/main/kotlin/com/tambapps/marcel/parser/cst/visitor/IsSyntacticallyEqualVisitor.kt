package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.eq
import com.tambapps.marcel.parser.cst.expression.AllInCstNode
import com.tambapps.marcel.parser.cst.expression.AnyInCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.FindInCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.InOperationCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.MapFilterCstNode
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
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
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
import com.tambapps.marcel.parser.cst.notEq
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
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode

/**
 * Visitor checking if the node supplied is equal to the visited node, in terms of parsing meaning
 */
class IsSyntacticallyEqualVisitor(
  val node: CstNode,
): ExpressionCstNodeVisitor<Boolean, Unit>, StatementCstNodeVisitor<Boolean> {

  override fun visit(
    node: BoolCstNode,
    smartCastType: Unit?
  ) = eqTo<BoolCstNode> { a -> a.value == node.value }

  override fun visit(
    node: DoubleCstNode,
    smartCastType: Unit?
  ) = eqTo<DoubleCstNode> { a -> a.value == node.value }

  override fun visit(
    node: FloatCstNode,
    smartCastType: Unit?
  ) = eqTo<FloatCstNode> { a -> a.value == node.value }

  override fun visit(
    node: IntCstNode,
    smartCastType: Unit?
  ) = eqTo<IntCstNode> { a -> a.value == node.value }

  override fun visit(
    node: LongCstNode,
    smartCastType: Unit?
  ) = eqTo<LongCstNode> { a -> a.value == node.value }

  override fun visit(
    node: NullCstNode,
    smartCastType: Unit?
  ) = eqTo<NullCstNode> { _ -> true }

  override fun visit(
    node: StringCstNode,
    smartCastType: Unit?
  ) = eqTo<StringCstNode> { a -> a.value == node.value }

  override fun visit(
    node: RegexCstNode,
    smartCastType: Unit?
  ) = eqTo<RegexCstNode> { a -> a.value == node.value && a.flags == node.flags }

  override fun visit(
    node: CharCstNode,
    smartCastType: Unit?
  ) = eqTo<CharCstNode> { a -> a.value == node.value }

  override fun visit(
    node: TemplateStringCstNode,
    smartCastType: Unit?
  ) = eqTo<TemplateStringCstNode> { a -> a.expressions eq node.expressions }

  override fun visit(
    node: MapCstNode,
    smartCastType: Unit?
  ) = eqTo<MapCstNode> { a ->
    if (a.entries.size != node.entries.size) return@eqTo false
    for (i in a.entries.indices) {
      val (k1, v1) = a.entries[i]
      val (k2, v2) = node.entries[i]
      if (k1 notEq k2) return@eqTo false
      if (v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: ArrayCstNode,
    smartCastType: Unit?
  ) = eqTo<ArrayCstNode> { a -> a.elements eq node.elements }

  override fun visit(
    node: MapFilterCstNode,
    smartCastType: Unit?
  ) = eqTo<MapFilterCstNode> { a ->
    inOpEq(a, node) && a.mapExpr eq node.mapExpr
  }

  override fun visit(
    node: AllInCstNode,
    smartCastType: Unit?
  ) = eqTo<AllInCstNode> { a ->
    inOpEq(a, node) && a.negate == node.negate
  }

  override fun visit(
    node: AnyInCstNode,
    smartCastType: Unit?
  ) = eqTo<AnyInCstNode> { a ->
    inOpEq(a, node) && a.negate == node.negate
  }

  override fun visit(
    node: FindInCstNode,
    smartCastType: Unit?
  )= eqTo<FindInCstNode> { a -> inOpEq(a, node) }

  override fun visit(
    node: UnaryMinusCstNode,
    smartCastType: Unit?
  ) = eqTo<UnaryMinusCstNode> { a -> a.expression eq node.expression }

  override fun visit(
    node: NotCstNode,
    smartCastType: Unit?
  ) = eqTo<NotCstNode> { a -> a.expression eq node.expression }

  override fun visit(
    node: WrappedExpressionCstNode,
    smartCastType: Unit?
  ) = eqTo<WrappedExpressionCstNode> { a -> a.expressionNode eq node.expressionNode }

  override fun visit(
    node: BinaryOperatorCstNode,
    smartCastType: Unit?
  ) = eqTo<BinaryOperatorCstNode> { a ->
    a.tokenType == node.tokenType && a.leftOperand eq node.leftOperand && a.rightOperand eq node.rightOperand
  }

  override fun visit(
    node: ElvisThrowCstNode,
    smartCastType: Unit?
  ) = eqTo<ElvisThrowCstNode> { a ->
    a.expression eq node.expression && a.throwableException eq node.throwableException
  }

  override fun visit(
    node: BinaryTypeOperatorCstNode,
    smartCastType: Unit?
  ) = eqTo<BinaryTypeOperatorCstNode> { a ->
    a.tokenType == node.tokenType && a.leftOperand eq node.leftOperand && a.rightOperand eq node.rightOperand
  }

  override fun visit(
    node: TernaryCstNode,
    smartCastType: Unit?
  ) = eqTo<TernaryCstNode> { a ->
    a.testExpressionNode eq node.testExpressionNode
        && a.trueExpressionNode eq node.trueExpressionNode
        && a.falseExpressionNode eq node.falseExpressionNode
  }

  override fun visit(
    node: ClassReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<ClassReferenceCstNode> { a -> a.type eq node.type }

  override fun visit(
    node: ThisReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<ThisReferenceCstNode> { true }

  override fun visit(
    node: SuperReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<SuperReferenceCstNode> { true }

  override fun visit(
    node: DirectFieldReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<DirectFieldReferenceCstNode> { it.value == node.value }

  override fun visit(
    node: IncrCstNode,
    smartCastType: Unit?
  ) = eqTo<IncrCstNode> {
    it.value == node.value
        && it.returnValueBefore == node.returnValueBefore
        && it.amount == node.amount
  }

  override fun visit(
    node: IndexAccessCstNode,
    smartCastType: Unit?
  ) = eqTo<IndexAccessCstNode> {
    it.ownerNode eq node.ownerNode
        && it.indexNodes eq node.indexNodes
        && it.isSafeAccess == node.isSafeAccess
  }

  override fun visit(
    node: ReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<ReferenceCstNode> { it.value == node.value }

  override fun visit(
    node: FunctionCallCstNode,
    smartCastType: Unit?
  ) = eqTo<FunctionCallCstNode> {
    if (it.value != node.value) return@eqTo false
    if (it.castType notEq node.castType) return@eqTo false
    if (it.positionalArgumentNodes notEq node.positionalArgumentNodes) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: SuperConstructorCallCstNode,
    smartCastType: Unit?
  ) = eqTo<SuperConstructorCallCstNode> {
    if (it.positionalArgumentNodes notEq node.positionalArgumentNodes) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: ThisConstructorCallCstNode,
    smartCastType: Unit?
  ) = eqTo<ThisConstructorCallCstNode> {
    if (it.positionalArgumentNodes notEq node.positionalArgumentNodes) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }


  override fun visit(
    node: NewInstanceCstNode,
    smartCastType: Unit?
  ) = eqTo<NewInstanceCstNode> {
    if (it.type notEq node.type) return@eqTo false
    if (it.positionalArgumentNodes notEq node.positionalArgumentNodes) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || v1 notEq v2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: WhenCstNode,
    smartCastType: Unit?
  ) = eqTo<WhenCstNode> {
    if (it.elseStatement notEq node.elseStatement) return@eqTo false
    if (it.branches.size != node.branches.size) return@eqTo false
    for (i in it.branches.indices) {
      val (expr1, stmt1) = it.branches[i]
      val (expr2, stmt2) = node.branches[i]
      if (expr1 notEq expr2 || stmt1 notEq stmt2) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: SwitchCstNode,
    smartCastType: Unit?
  ) = visit(node as WhenCstNode) && eqTo<SwitchCstNode> {
    it.varDeclaration eq node.varDeclaration
        && it.switchExpression eq node.switchExpression
  }

  override fun visit(
    node: LambdaCstNode,
    smartCastType: Unit?
  ) = false // too complex to compare  here

  override fun visit(
    node: AsyncBlockCstNode,
    smartCastType: Unit?
  ) = eqTo<AsyncBlockCstNode> { it.block eq node.block }

  override fun visit(
    node: TruthyVariableDeclarationCstNode,
    smartCastType: Unit?
  ) = eqTo<TruthyVariableDeclarationCstNode> {
    if (it.type notEq node.type) return@eqTo false
    if (it.value != node.value) return@eqTo false
    if (it.expression notEq node.expression) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ExpressionStatementCstNode) = eqTo<ExpressionStatementCstNode> { it.expressionNode eq node.expressionNode }

  override fun visit(node: ReturnCstNode) = eqTo<ReturnCstNode> { it.expressionNode eq node.expressionNode }

  override fun visit(node: VariableDeclarationCstNode) = eqTo<VariableDeclarationCstNode> {
    if (it.type notEq node.type) return@eqTo false
    if (it.value != node.value) return@eqTo false
    if (it.expressionNode notEq node.expressionNode) return@eqTo false
    if (it.isNullable != node.isNullable) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: MultiVarDeclarationCstNode)= eqTo<MultiVarDeclarationCstNode> {
    if (node.declarations.size != it.declarations.size) return@eqTo false
    for (i in node.declarations.indices) {
      val triple1 = it.declarations[i]
      val triple2 = node.declarations[i]
      if (triple1 == null && triple2 == null) continue
      if (triple1 == null || triple2 == null) return@eqTo false
      val (type1, name1, nullable1) = triple1
      val (type2, name2, nullable2) = triple2
      if (type1 notEq type2) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (nullable1 != nullable2) return@eqTo false
    }
    if (it.expressionNode notEq node.expressionNode) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: IfStatementCstNode) = eqTo<IfStatementCstNode> {
    if (it.condition notEq node.condition) return@eqTo false
    if (it.trueStatementNode notEq node.trueStatementNode) return@eqTo false
    if (it.falseStatementNode notEq node.falseStatementNode) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForInCstNode) = eqTo<ForInCstNode> {
    if (it.varType notEq node.varType) return@eqTo false
    if (it.varName != node.varName) return@eqTo false
    if (it.isVarNullable != node.isVarNullable) return@eqTo false
    if (it.inNode notEq node.inNode) return@eqTo false
    if (it.statementNode notEq node.statementNode) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForInMultiVarCstNode) = eqTo<ForInMultiVarCstNode> {
    if (it.declarations.size != node.declarations.size) return@eqTo false
    for (i in it.declarations.indices) {
      val (type1, name1, nullable1) = it.declarations[i]
      val (type2, name2, nullable2) = node.declarations[i]
      if (type1 notEq type2) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (nullable1 != nullable2) return@eqTo false
    }
    if (it.inNode notEq node.inNode) return@eqTo false
    if (it.statementNode notEq node.statementNode) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForVarCstNode) = eqTo<ForVarCstNode> {
    if (it.varDecl notEq node.varDecl) return@eqTo false
    if (it.condition notEq node.condition) return@eqTo false
    if (it.iteratorStatement notEq node.iteratorStatement) return@eqTo false
    if (it.bodyStatement notEq node.bodyStatement) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: WhileCstNode) = eqTo<WhileCstNode> {
    if (it.condition notEq node.condition) return@eqTo false
    if (it.statement notEq node.statement) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: DoWhileStatementCstNode) = eqTo<DoWhileStatementCstNode> {
    if (it.condition notEq node.condition) return@eqTo false
    if (it.statement notEq node.statement) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: BlockCstNode) = eqTo<BlockCstNode> { it.statements eq node.statements }

  override fun visit(node: BreakCstNode) = this.node is BreakCstNode

  override fun visit(node: ContinueCstNode) = this.node is ContinueCstNode

  override fun visit(node: ThrowCstNode) = eqTo<ThrowCstNode> { it.expression eq node.expression }

  override fun visit(node: TryCatchCstNode) = eqTo<TryCatchCstNode> {
    if (it.tryNode notEq node.tryNode) return@eqTo false
    if (it.resources notEq node.resources) return@eqTo false
    if (it.catchNodes.size != node.catchNodes.size) return@eqTo false
    for (i in it.catchNodes.indices) {
      val (types1, name1, stmt1) = it.catchNodes[i]
      val (types2, name2, stmt2) = node.catchNodes[i]
      if (types1 notEq types2) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (stmt1 notEq stmt2) return@eqTo false
    }
    if (it.finallyNode notEq node.finallyNode) return@eqTo false
    return@eqTo true
  }

  private fun inOpEq(a: InOperationCstNode, b: InOperationCstNode): Boolean {
    if (a.varType notEq b.varType) return false
    if (a.varName != b.varName) return false
    if (a.inExpr notEq b.inExpr) return false
    if (a.filterExpr notEq b.filterExpr) return false
    return true
  }

  private inline fun <reified T> eqTo(compare: (T) -> Boolean): Boolean {
    if (node !is T) return false
    return compare(node)
  }
}