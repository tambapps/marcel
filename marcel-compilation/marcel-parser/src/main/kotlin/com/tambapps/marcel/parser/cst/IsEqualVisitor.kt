package com.tambapps.marcel.parser.cst

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
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

/**
 * Visitor checking if the node supplied is equal to the visited node, in terms of parsing meaning
 */
class IsEqualVisitor(
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
  ) = eqTo<TemplateStringCstNode> { a -> eq(a.expressions, node.expressions) }

  override fun visit(
    node: MapCstNode,
    smartCastType: Unit?
  ) = eqTo<MapCstNode> { a ->
    if (a.entries.size != node.entries.size) return@eqTo false
    for (i in a.entries.indices) {
      val (k1, v1) = a.entries[i]
      val (k2, v2) = node.entries[i]
      if (!eq(k1, k2)) return@eqTo false
      if (!eq(v1, v2)) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: ArrayCstNode,
    smartCastType: Unit?
  ) = eqTo<ArrayCstNode> { a -> eq(a.elements, node.elements) }

  override fun visit(
    node: MapFilterCstNode,
    smartCastType: Unit?
  ) = eqTo<MapFilterCstNode> { a ->
    inOpEq(a, node) && eq(a.mapExpr, node.mapExpr)
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
  ) = eqTo<UnaryMinusCstNode> { a -> eq(a.expression, node.expression) }

  override fun visit(
    node: NotCstNode,
    smartCastType: Unit?
  ) = eqTo<NotCstNode> { a -> eq(a.expression, node.expression) }

  override fun visit(
    node: WrappedExpressionCstNode,
    smartCastType: Unit?
  ) = eqTo<WrappedExpressionCstNode> { a -> eq(a.expressionNode, node.expressionNode) }

  override fun visit(
    node: BinaryOperatorCstNode,
    smartCastType: Unit?
  ) = eqTo<BinaryOperatorCstNode> { a ->

    eq(a.leftOperand, node.leftOperand)
  }

  override fun visit(
    node: ElvisThrowCstNode,
    smartCastType: Unit?
  ) = eqTo<ElvisThrowCstNode> { a ->
    eq(a.expression, node.expression) && eq(a.throwableException, node.throwableException)
  }

  override fun visit(
    node: BinaryTypeOperatorCstNode,
    smartCastType: Unit?
  ) = eqTo<BinaryTypeOperatorCstNode> { a ->
    a.tokenType == node.tokenType && eq(a.leftOperand, node.leftOperand) && eq(a.rightOperand, node.rightOperand)
  }

  override fun visit(
    node: TernaryCstNode,
    smartCastType: Unit?
  ) = eqTo<TernaryCstNode> { a ->
    eq(a.testExpressionNode, node.testExpressionNode)
        && eq(a.trueExpressionNode, node.trueExpressionNode)
        && eq(a.falseExpressionNode, node.falseExpressionNode)
  }

  override fun visit(
    node: ClassReferenceCstNode,
    smartCastType: Unit?
  ) = eqTo<ClassReferenceCstNode> { a -> eq(a.type, node.type) }

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
    eq(it.ownerNode, node.ownerNode)
        && eq(it.indexNodes, node.indexNodes)
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
    if (!eq(it.castType, node.castType)) return@eqTo false
    if (!eq(it.positionalArgumentNodes, node.positionalArgumentNodes)) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || !eq(v1, v2)) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: SuperConstructorCallCstNode,
    smartCastType: Unit?
  ) = eqTo<SuperConstructorCallCstNode> {
    if (!eq(it.positionalArgumentNodes, node.positionalArgumentNodes)) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || !eq(v1, v2)) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: ThisConstructorCallCstNode,
    smartCastType: Unit?
  ) = eqTo<ThisConstructorCallCstNode> {
    if (!eq(it.positionalArgumentNodes, node.positionalArgumentNodes)) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || !eq(v1, v2)) return@eqTo false
    }
    return@eqTo true
  }


  override fun visit(
    node: NewInstanceCstNode,
    smartCastType: Unit?
  ) = eqTo<NewInstanceCstNode> {
    if (!eq(it.type, node.type)) return@eqTo false
    if (!eq(it.positionalArgumentNodes, node.positionalArgumentNodes)) return@eqTo false
    if (it.namedArgumentNodes.size != node.namedArgumentNodes.size) return@eqTo false
    for (i in it.namedArgumentNodes.indices) {
      val (k1, v1) = it.namedArgumentNodes[i]
      val (k2, v2) = node.namedArgumentNodes[i]
      if (k1 != k2 || !eq(v1, v2)) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: WhenCstNode,
    smartCastType: Unit?
  ) = eqTo<WhenCstNode> {
    if (!eq(it.elseStatement, node.elseStatement)) return@eqTo false
    if (it.branches.size != node.branches.size) return@eqTo false
    for (i in it.branches.indices) {
      val (expr1, stmt1) = it.branches[i]
      val (expr2, stmt2) = node.branches[i]
      if (!eq(expr1, expr2) || !eq(stmt1, stmt2)) return@eqTo false
    }
    return@eqTo true
  }

  override fun visit(
    node: SwitchCstNode,
    smartCastType: Unit?
  ) = visit(node as WhenCstNode) && eqTo<SwitchCstNode> {
    eq(it.varDeclaration, node.varDeclaration)
        && eq(it.switchExpression, node.switchExpression)
  }

  override fun visit(
    node: LambdaCstNode,
    smartCastType: Unit?
  ) = false // too complex to compare  here

  override fun visit(
    node: AsyncBlockCstNode,
    smartCastType: Unit?
  ) = eqTo<AsyncBlockCstNode> { eq(it.block, node.block) }

  override fun visit(
    node: TruthyVariableDeclarationCstNode,
    smartCastType: Unit?
  ) = eqTo<TruthyVariableDeclarationCstNode> {
    if (!eq(it.type, node.type)) return@eqTo false
    if (it.value != node.value) return@eqTo false
    if (!eq(it.expression, node.expression)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ExpressionStatementCstNode) = eqTo<ExpressionStatementCstNode> { eq(it.expressionNode, node.expressionNode) }

  override fun visit(node: ReturnCstNode) = eqTo<ReturnCstNode> { eq(it.expressionNode, node.expressionNode) }

  override fun visit(node: VariableDeclarationCstNode) = eqTo<VariableDeclarationCstNode> {
    if (!eq(it.type, node.type)) return@eqTo false
    if (it.value != node.value) return@eqTo false
    if (!eq(it.expressionNode, node.expressionNode)) return@eqTo false
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
      if (!eq(type1, type2)) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (nullable1 != nullable2) return@eqTo false
    }
    if (!eq(it.expressionNode, node.expressionNode)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: IfStatementCstNode) = eqTo<IfStatementCstNode> {
    if (!eq(it.condition, node.condition)) return@eqTo false
    if (!eq(it.trueStatementNode, node.trueStatementNode)) return@eqTo false
    if (!eq(it.falseStatementNode, node.falseStatementNode)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForInCstNode) = eqTo<ForInCstNode> {
    if (!eq(it.varType, node.varType)) return@eqTo false
    if (it.varName != node.varName) return@eqTo false
    if (it.isVarNullable != node.isVarNullable) return@eqTo false
    if (!eq(it.inNode, node.inNode)) return@eqTo false
    if (!eq(it.statementNode, node.statementNode)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForInMultiVarCstNode) = eqTo<ForInMultiVarCstNode> {
    if (it.declarations.size != node.declarations.size) return@eqTo false
    for (i in it.declarations.indices) {
      val (type1, name1, nullable1) = it.declarations[i]
      val (type2, name2, nullable2) = node.declarations[i]
      if (!eq(type1, type2)) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (nullable1 != nullable2) return@eqTo false
    }
    if (!eq(it.inNode, node.inNode)) return@eqTo false
    if (!eq(it.statementNode, node.statementNode)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: ForVarCstNode) = eqTo<ForVarCstNode> {
    if (!eq(it.varDecl, node.varDecl)) return@eqTo false
    if (!eq(it.condition, node.condition)) return@eqTo false
    if (!eq(it.iteratorStatement, node.iteratorStatement)) return@eqTo false
    if (!eq(it.bodyStatement, node.bodyStatement)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: WhileCstNode) = eqTo<WhileCstNode> {
    if (!eq(it.condition, node.condition)) return@eqTo false
    if (!eq(it.statement, node.statement)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: DoWhileStatementCstNode) = eqTo<DoWhileStatementCstNode> {
    if (!eq(it.condition, node.condition)) return@eqTo false
    if (!eq(it.statement, node.statement)) return@eqTo false
    return@eqTo true
  }

  override fun visit(node: BlockCstNode) = eqTo<BlockCstNode> { eq(it.statements, node.statements) }

  override fun visit(node: BreakCstNode) = this.node is BreakCstNode

  override fun visit(node: ContinueCstNode) = this.node is ContinueCstNode

  override fun visit(node: ThrowCstNode) = eqTo<ThrowCstNode> { eq(it.expression, node.expression) }

  override fun visit(node: TryCatchCstNode) = eqTo<TryCatchCstNode> {
    if (!eq(it.tryNode, node.tryNode)) return@eqTo false
    if (!eq(it.resources, node.resources)) return@eqTo false
    if (it.catchNodes.size != node.catchNodes.size) return@eqTo false
    for (i in it.catchNodes.indices) {
      val (types1, name1, stmt1) = it.catchNodes[i]
      val (types2, name2, stmt2) = node.catchNodes[i]
      if (!eq(types1, types2)) return@eqTo false
      if (name1 != name2) return@eqTo false
      if (!eq(stmt1, stmt2)) return@eqTo false
    }
    if (!eq(it.finallyNode, node.finallyNode)) return@eqTo false
    return@eqTo true
  }

  fun <T: IdentifiableCstNode, U: IdentifiableCstNode> eq(nodes1: List<T>, nodes2: List<U>): Boolean {
    if (nodes1.size != nodes2.size) return false
    for (i in nodes1.indices) {
      if (!eq(nodes1[i], nodes2[i])) return false
    }
    return true
  }

  private fun eq(n1: IdentifiableCstNode?, n2: IdentifiableCstNode?): Boolean = when {
    n1 == null && n2 == null -> true
    n1 == null || n2 == null -> false
    else -> n1.isEqualTo(n2)
  }

  private fun inOpEq(a: InOperationCstNode, b: InOperationCstNode): Boolean {
    if (!eq(a.varType, b.varType)) return false
    if (a.varName != b.varName) return false
    if (!eq(a.inExpr, b.inExpr)) return false
    if (!eq(a.filterExpr, b.filterExpr)) return false
    return true
  }

  private inline fun <reified T> eqTo(compare: (T) -> Boolean): Boolean {
    if (node !is T) return false
    return compare(node)
  }
}