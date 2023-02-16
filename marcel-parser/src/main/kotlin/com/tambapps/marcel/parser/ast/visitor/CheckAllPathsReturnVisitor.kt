package com.tambapps.marcel.parser.ast.visitor

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.AndOperator
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.CharConstantNode
import com.tambapps.marcel.parser.ast.expression.ComparisonOperatorNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.ElseBranchNode
import com.tambapps.marcel.parser.ast.expression.ElvisOperator
import com.tambapps.marcel.parser.ast.expression.FieldAssignmentNode
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.InvokeAccessOperator
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
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.expression.RangeNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.RightShiftOperator
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.ast.expression.StringNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.SwitchBranchNode
import com.tambapps.marcel.parser.ast.expression.SwitchNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.ToStringNode
import com.tambapps.marcel.parser.ast.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

class CheckAllPathsReturnVisitor: AstNodeVisitor<Boolean> {
  override fun visit(integer: IntConstantNode): Boolean {
    return false
  }

  override fun visit(longConstantNode: LongConstantNode): Boolean {
    return false
  }

  override fun visit(floatConstantNode: FloatConstantNode): Boolean {
    return false
  }

  override fun visit(doubleConstantNode: DoubleConstantNode): Boolean {
    return false
  }

  override fun visit(charNode: CharConstantNode): Boolean {
    return false
  }

  override fun visit(operator: MulOperator): Boolean {
    return false
  }

  override fun visit(operator: TernaryNode): Boolean {
    return false
  }

  override fun visit(elvisOperator: ElvisOperator): Boolean {
    return false
  }

  override fun visit(fCall: FunctionCallNode): Boolean {
    return false
  }

  override fun visit(fCall: ConstructorCallNode): Boolean {
    return false
  }

  override fun visit(fCall: SuperConstructorCallNode): Boolean {
    return false
  }

  override fun visit(operator: DivOperator): Boolean {
    return false
  }

  override fun visit(operator: PlusOperator): Boolean {
    return false
  }

  override fun visit(operator: MinusOperator): Boolean {
    return false
  }

  override fun visit(operator: PowOperator): Boolean {
    return false
  }

  override fun visit(rightShiftOperator: RightShiftOperator): Boolean {
    return false
  }

  override fun visit(leftShiftOperator: LeftShiftOperator): Boolean {
    return false
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode): Boolean {
    return false
  }

  override fun visit(fieldAssignmentNode: FieldAssignmentNode): Boolean {
    return false
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode): Boolean {
    return false
  }

  override fun visit(referenceExpression: ReferenceExpression): Boolean {
    return false
  }

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression): Boolean {
    return false
  }

  override fun visit(unaryMinus: UnaryMinus): Boolean {
    return false
  }

  override fun visit(unaryPlus: UnaryPlus): Boolean {
    return false
  }

  override fun visit(blockNode: BlockNode): Boolean {
    return if (blockNode.statements.isNotEmpty()) blockNode.statements.last().accept(this)
    else false
  }

  override fun visit(blockNode: FunctionBlockNode): Boolean {
    return if (blockNode.statements.isNotEmpty()) blockNode.statements.last().accept(this)
    else false
  }

  override fun visit(lambdaNode: LambdaNode): Boolean {
    return false
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode): Boolean {
    return expressionStatementNode is ReturnNode
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode): Boolean {
    return false
  }

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode): Boolean {
    return false
  }

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode): Boolean {
    return false
  }

  override fun visit(returnNode: ReturnNode): Boolean {
    return true
  }

  override fun visit(voidExpression: VoidExpression): Boolean {
    return false
  }

  override fun visit(stringNode: StringNode): Boolean {
    return false
  }

  override fun visit(stringConstantNode: StringConstantNode): Boolean {
    return false
  }

  override fun visit(asNode: AsNode): Boolean {
    return false
  }

  override fun visit(toStringNode: ToStringNode): Boolean {
    return false
  }

  override fun visit(accessOperator: InvokeAccessOperator): Boolean {
    return false
  }

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator): Boolean {
    return false
  }

  override fun visit(booleanConstantNode: BooleanConstantNode): Boolean {
    return false
  }

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode): Boolean {
    return false
  }

  override fun visit(andOperator: AndOperator): Boolean {
    return false
  }

  override fun visit(orOperator: OrOperator): Boolean {
    return false
  }

  override fun visit(notNode: NotNode): Boolean {
    return false
  }

  override fun visit(ifStatementNode: IfStatementNode): Boolean {
    if (ifStatementNode.falseStatementNode == null) return false
    return ifStatementNode.trueStatementNode.accept(this) && ifStatementNode.falseStatementNode!!.accept(this)
  }

  override fun visit(forStatement: ForStatement): Boolean {
    return false
  }

  override fun visit(forInStatement: ForInStatement): Boolean {
    return false
  }

  override fun visit(whileStatement: WhileStatement): Boolean {
    return false
  }

  override fun visit(booleanExpression: BooleanExpressionNode): Boolean {
    return false
  }

  override fun visit(nullValueNode: NullValueNode): Boolean {
    return false
  }

  override fun visit(incrNode: IncrNode): Boolean {
    return false
  }

  override fun visit(breakLoopNode: BreakLoopNode): Boolean {
    return false
  }

  override fun visit(continueLoopNode: ContinueLoopNode): Boolean {
    return false
  }

  override fun visit(rangeNode: RangeNode): Boolean {
    return false
  }

  override fun visit(literalListNode: LiteralArrayNode): Boolean {
    return false
  }

  override fun visit(literalMapNode: LiteralMapNode): Boolean {
    return false
  }

  override fun visit(switchBranch: SwitchBranchNode): Boolean {
    return switchBranch.statementNode.accept(this)
  }

  override fun visit(switchNode: SwitchNode): Boolean {
    return switchNode.branches.any { it is ElseBranchNode } && switchNode.branches.all { it.accept(this) }
  }
}