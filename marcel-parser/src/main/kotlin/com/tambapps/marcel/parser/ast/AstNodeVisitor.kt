package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

interface AstNodeVisitor<T> {

  fun visit(integer: IntConstantNode): T
  fun visit(longConstantNode: LongConstantNode): T
  fun visit(floatConstantNode: FloatConstantNode): T
  fun visit(doubleConstantNode: DoubleConstantNode): T
  fun visit(charNode: CharConstantNode): T

  fun visit(operator: MulOperator): T
  fun visit(operator: TernaryNode): T
  fun visit(elvisOperator: ElvisOperator): T

  fun visit(fCall: FunctionCallNode): T
  fun visit(fCall: ConstructorCallNode): T
  fun visit(fCall: SuperConstructorCallNode): T
  fun visit(operator: DivOperator): T
  fun visit(operator: PlusOperator): T
  fun visit(operator: MinusOperator): T
  fun visit(operator: PowOperator): T
  fun visit(rightShiftOperator: RightShiftOperator): T
  fun visit(leftShiftOperator: LeftShiftOperator): T
  fun visit(variableAssignmentNode: VariableAssignmentNode): T
  fun visit(fieldAssignmentNode: FieldAssignmentNode): T

  fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode): T

  fun visit(referenceExpression: ReferenceExpression): T
  fun visit(indexedReferenceExpression: IndexedReferenceExpression): T

  fun visit(unaryMinus: UnaryMinus): T
  fun visit(unaryPlus: UnaryPlus): T
  fun visit(blockNode: BlockNode): T
  fun visit(blockNode: FunctionBlockNode): T
  fun visit(lambdaNode: LambdaNode): T

  fun visit(expressionStatementNode: ExpressionStatementNode): T
  fun visit(variableDeclarationNode: VariableDeclarationNode): T
  fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode): T
  fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode): T

  fun visit(returnNode: ReturnNode): T
  fun visit(voidExpression: VoidExpression): T
  fun visit(stringNode: StringNode): T
  fun visit(stringConstantNode: StringConstantNode): T
  fun visit(asNode: AsNode): T

  fun visit(toStringNode: ToStringNode): T
  fun visit(accessOperator: InvokeAccessOperator): T
  fun visit(getFieldAccessOperator: GetFieldAccessOperator): T

  fun visit(booleanConstantNode: BooleanConstantNode): T
  fun visit(comparisonOperatorNode: ComparisonOperatorNode): T
  fun visit(andOperator: AndOperator): T
  fun visit(orOperator: OrOperator): T

  fun visit(notNode: NotNode): T
  fun visit(ifStatementNode: IfStatementNode): T
  fun visit(forStatement: ForStatement): T
  fun visit(forInStatement: ForInStatement): T

  fun visit(whileStatement: WhileStatement): T
  fun visit(booleanExpression: BooleanExpressionNode): T
  fun visit(nullValueNode: NullValueNode): T
  fun visit(incrNode: IncrNode): T
  fun visit(breakLoopNode: BreakLoopNode): T
  fun visit(continueLoopNode: ContinueLoopNode): T
  fun visit(rangeNode: RangeNode): T
  fun visit(literalListNode: LiteralArrayNode): T
  fun visit(literalMapNode: LiteralMapNode): T
  fun visit(switchBranch: SwitchBranchNode): T
  fun visit(switchNode: SwitchNode): T
  fun visit(whenBranchNode: WhenBranchNode): T
  fun visit(whenNode: WhenNode): T
  fun visit(isOperator: IsOperator): T
  fun visit(isNotOperator: IsNotOperator): T

}