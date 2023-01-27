package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

interface AstNodeVisitor {

  fun visit(integer: IntConstantNode)
  fun visit(longConstantNode: LongConstantNode)
  fun visit(floatConstantNode: FloatConstantNode)
  fun visit(doubleConstantNode: DoubleConstantNode)
  fun visit(operator: MulOperator)
  fun visit(operator: TernaryNode)
  fun visit(fCall: FunctionCallNode)
  fun visit(fCall: ConstructorCallNode)
  fun visit(fCall: SuperConstructorCallNode)
  fun visit(operator: DivOperator)
  fun visit(operator: PlusOperator)
  fun visit(operator: MinusOperator)
  fun visit(operator: PowOperator)
  fun visit(variableAssignmentNode: VariableAssignmentNode)
  fun visit(referenceExpression: ReferenceExpression)
  fun visit(unaryMinus: UnaryMinus)
  fun visit(unaryPlus: UnaryPlus)
  fun visit(blockNode: BlockNode)
  fun visit(blockNode: FunctionBlockNode)

  fun visit(expressionStatementNode: ExpressionStatementNode)
  fun visit(variableDeclarationNode: VariableDeclarationNode)
  fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode)
  fun visit(returnNode: ReturnNode)
  fun visit(voidExpression: VoidExpression)
  fun visit(stringNode: StringNode)
  fun visit(stringConstantNode: StringConstantNode)
  fun visit(toStringNode: ToStringNode)
  fun visit(accessOperator: InvokeAccessOperator)
  fun visit(getFieldAccessOperator: GetFieldAccessOperator)

  fun visit(booleanConstantNode: BooleanConstantNode)
  fun visit(comparisonOperator: ComparisonOperatorNode)
  fun visit(andOperator: AndOperator)
  fun visit(orOperator: OrOperator)

  fun visit(notNode: NotNode)
  fun visit(ifStatementNode: IfStatementNode)
  fun visit(forStatement: ForStatement)
  fun visit(forInStatement: ForInStatement)

  fun visit(whileStatement: WhileStatement)
  fun visit(booleanExpression: BooleanExpressionNode)
  fun visit(nullValueNode: NullValueNode)
  fun visit(incrNode: IncrNode)
  fun visit(breakLoopNode: BreakLoopNode)
  fun visit(continueLoopNode: ContinueLoopNode)
  fun visit(rangeNode: RangeNode)
  fun visit(literalListNode: LiteralArrayNode)
  fun visit(literalMapNode: LiteralMapNode)

}