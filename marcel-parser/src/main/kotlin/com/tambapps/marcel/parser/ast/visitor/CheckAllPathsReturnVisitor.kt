package com.tambapps.marcel.parser.ast.visitor

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.AndOperator
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ByteConstantNode
import com.tambapps.marcel.parser.ast.expression.CharConstantNode
import com.tambapps.marcel.parser.ast.expression.ComparisonOperatorNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.ElvisOperator
import com.tambapps.marcel.parser.ast.expression.FieldAssignmentNode
import com.tambapps.marcel.parser.ast.expression.FindOperator
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.InvokeAccessOperator
import com.tambapps.marcel.parser.ast.expression.IsNotOperator
import com.tambapps.marcel.parser.ast.expression.IsOperator
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.LeftShiftOperator
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NotNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.OrOperator
import com.tambapps.marcel.parser.ast.expression.LiteralPatternNode
import com.tambapps.marcel.parser.ast.expression.NamedParametersConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.expression.RangeNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.RightShiftOperator
import com.tambapps.marcel.parser.ast.expression.ShortConstantNode
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.ast.expression.StringNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.SuperReference
import com.tambapps.marcel.parser.ast.expression.SwitchBranchNode
import com.tambapps.marcel.parser.ast.expression.SwitchNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.ThisReference
import com.tambapps.marcel.parser.ast.expression.ToStringNode
import com.tambapps.marcel.parser.ast.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.ast.expression.WhenBranchNode
import com.tambapps.marcel.parser.ast.expression.WhenNode
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
  override fun visit(integer: IntConstantNode) = false

  override fun visit(longConstantNode: LongConstantNode) = false

  override fun visit(floatConstantNode: FloatConstantNode) = false

  override fun visit(doubleConstantNode: DoubleConstantNode) = false

  override fun visit(charNode: CharConstantNode) = false

  override fun visit(operator: MulOperator) = false

  override fun visit(operator: TernaryNode) = false

  override fun visit(elvisOperator: ElvisOperator) = false

  override fun visit(fCall: FunctionCallNode) = false

  override fun visit(fCall: ConstructorCallNode) = false

  override fun visit(fCall: NamedParametersConstructorCallNode) = false
  override fun visit(fCall: SuperConstructorCallNode) = false

  override fun visit(operator: DivOperator) = false

  override fun visit(operator: PlusOperator) = false

  override fun visit(operator: MinusOperator) = false

  override fun visit(operator: PowOperator) = false

  override fun visit(rightShiftOperator: RightShiftOperator) = false

  override fun visit(leftShiftOperator: LeftShiftOperator) = false

  override fun visit(variableAssignmentNode: VariableAssignmentNode) = false

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) = false

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) = false

  override fun visit(referenceExpression: ReferenceExpression) = false

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) = false

  override fun visit(unaryMinus: UnaryMinus) = false

  override fun visit(unaryPlus: UnaryPlus) = false

  override fun visit(blockNode: BlockNode): Boolean {
    return if (blockNode.statements.isNotEmpty()) blockNode.statements.last().accept(this)
    else false
  }

  override fun visit(blockNode: FunctionBlockNode): Boolean {
    return if (blockNode.statements.isNotEmpty()) blockNode.statements.last().accept(this)
    else false
  }

  override fun visit(lambdaNode: LambdaNode) = false

  override fun visit(expressionStatementNode: ExpressionStatementNode): Boolean {
    return expressionStatementNode is ReturnNode
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) = false

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) = false

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) = false

  override fun visit(returnNode: ReturnNode): Boolean {
    return true
  }

  override fun visit(voidExpression: VoidExpression) = false

  override fun visit(stringNode: StringNode) = false

  override fun visit(stringConstantNode: StringConstantNode) = false

  override fun visit(asNode: AsNode) = false

  override fun visit(toStringNode: ToStringNode) = false

  override fun visit(accessOperator: InvokeAccessOperator) = false

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) = false

  override fun visit(booleanConstantNode: BooleanConstantNode) = false

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) = false

  override fun visit(andOperator: AndOperator) = false

  override fun visit(orOperator: OrOperator) = false

  override fun visit(notNode: NotNode) = false

  override fun visit(ifStatementNode: IfStatementNode): Boolean {
    if (ifStatementNode.falseStatementNode == null) return false
    return ifStatementNode.trueStatementNode.accept(this) && ifStatementNode.falseStatementNode!!.accept(this)
  }

  override fun visit(forStatement: ForStatement) = false

  override fun visit(tryCatchNode: TryCatchNode): Boolean {
    return tryCatchNode.tryStatementNode.accept(this) && (
        tryCatchNode.catchNodes.isEmpty() || tryCatchNode.catchNodes.all { it.statementNode.accept(this) }
        )
  }
  override fun visit(forInStatement: ForInStatement) = false

  override fun visit(whileStatement: WhileStatement) = false

  override fun visit(booleanExpression: BooleanExpressionNode) = false

  override fun visit(nullValueNode: NullValueNode) = false

  override fun visit(incrNode: IncrNode) = false

  override fun visit(breakLoopNode: BreakLoopNode) = false

  override fun visit(continueLoopNode: ContinueLoopNode) = false

  override fun visit(rangeNode: RangeNode) = false

  override fun visit(literalListNode: LiteralArrayNode) = false

  override fun visit(literalMapNode: LiteralMapNode) = false

  override fun visit(switchBranch: SwitchBranchNode): Boolean {
    return switchBranch.statementNode.accept(this)
  }

  override fun visit(switchNode: SwitchNode): Boolean {
    return switchNode.elseStatement != null && switchNode.branches.all { it.accept(this) }
  }

  override fun visit(whenBranchNode: WhenBranchNode): Boolean {
    return whenBranchNode.statementNode.accept(this)
  }

  override fun visit(whenNode: WhenNode): Boolean {
    return whenNode.elseStatement != null && whenNode.branches.all { it.accept(this) }
  }

  override fun visit(isOperator: IsOperator) = false
  override fun visit(isNotOperator: IsNotOperator) = false

  override fun visit(shortConstantNode: ShortConstantNode) = false

  override fun visit(byteConstantNode: ByteConstantNode) = false

  override fun visit(superReference: SuperReference) = false

  override fun visit(thisReference: ThisReference) = false
  override fun visit(patternValueNode: LiteralPatternNode) = false

  override fun visit(findOperator: FindOperator) = false

}