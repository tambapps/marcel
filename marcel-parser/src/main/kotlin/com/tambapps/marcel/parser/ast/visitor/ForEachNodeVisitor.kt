package com.tambapps.marcel.parser.ast.visitor

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.AndOperator
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.BinaryOperatorNode
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
import com.tambapps.marcel.parser.ast.expression.NamedParametersFunctionCall
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
import com.tambapps.marcel.parser.ast.expression.UnaryOperator
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
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

class ForEachNodeVisitor(private val consumer: (AstNode) -> Unit): AstNodeVisitor<Unit> {


  override fun visit(integer: IntConstantNode) = consumer.invoke(integer)

  override fun visit(longConstantNode: LongConstantNode) = consumer.invoke(longConstantNode)

  override fun visit(floatConstantNode: FloatConstantNode) = consumer.invoke(floatConstantNode)

  override fun visit(doubleConstantNode: DoubleConstantNode) = consumer.invoke(doubleConstantNode)

  override fun visit(charNode: CharConstantNode) = consumer.invoke(charNode)

  override fun visit(operator: MulOperator) = visitBinaryOperator(operator)
  override fun visit(findOperator: FindOperator) = visitBinaryOperator(findOperator)

  private fun visitBinaryOperator(operator: BinaryOperatorNode) {
    consumer.invoke(operator)
    operator.leftOperand.accept(this)
    operator.rightOperand.accept(this)
  }

  override fun visit(operator: TernaryNode) {
    consumer.invoke(operator)
    operator.boolExpression.accept(this)
    operator.trueExpression.accept(this)
    operator.falseExpression.accept(this)
  }

  override fun visit(elvisOperator: ElvisOperator) = visitBinaryOperator(elvisOperator)

  override fun visit(fCall: FunctionCallNode) {
    consumer.invoke(fCall)
    if (fCall is NamedParametersFunctionCall) fCall.namedArguments.forEach { it.valueExpression.accept(this) }
    else fCall.arguments.forEach { it.accept(this) }
  }

  override fun visit(fCall: ConstructorCallNode) {
    consumer.invoke(fCall)
    fCall.arguments.forEach { it.accept(this) }
  }

  override fun visit(fCall: SuperConstructorCallNode) {
    consumer.invoke(fCall)
    fCall.arguments.forEach { it.accept(this) }
  }

  override fun visit(fCall: NamedParametersConstructorCallNode) {
    consumer.invoke(fCall)
    fCall.namedArguments.forEach { it.valueExpression.accept(this) }
  }

  override fun visit(operator: DivOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PlusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: MinusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PowOperator) = visitBinaryOperator(operator)

  override fun visit(rightShiftOperator: RightShiftOperator) = visitBinaryOperator(rightShiftOperator)

  override fun visit(leftShiftOperator: LeftShiftOperator) = visitBinaryOperator(leftShiftOperator)

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    consumer.invoke(variableAssignmentNode)
    variableAssignmentNode.expression.accept(this)
  }

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) {
    consumer.invoke(fieldAssignmentNode)
    fieldAssignmentNode.fieldNode.accept(this)
    fieldAssignmentNode.expression.accept(this)
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) {
    consumer.invoke(indexedVariableAssignmentNode)
    indexedVariableAssignmentNode.indexedReference.accept(this)
    indexedVariableAssignmentNode.expression.accept(this)
  }

  override fun visit(referenceExpression: ReferenceExpression) = consumer.invoke(referenceExpression)

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) {
    consumer.invoke(indexedReferenceExpression)
    indexedReferenceExpression.indexArguments.forEach { it.accept(this) }
  }

  override fun visit(unaryMinus: UnaryMinus) = visitUnaryOperator(unaryMinus)

  private fun visitUnaryOperator(unaryOperator: UnaryOperator) {
    consumer.invoke(unaryOperator)
    unaryOperator.operand.accept(this)
  }

  override fun visit(unaryPlus: UnaryPlus) = visitUnaryOperator(unaryPlus)

  override fun visit(blockNode: BlockNode) {
    consumer.invoke(blockNode)
    blockNode.statements.forEach { it.accept(this) }
  }

  override fun visit(blockNode: FunctionBlockNode) {
    consumer.invoke(blockNode)
    blockNode.statements.forEach { it.accept(this) }
  }

  override fun visit(lambdaNode: LambdaNode) {
    consumer.invoke(lambdaNode)
    lambdaNode.blockNode.accept(this)
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    consumer.invoke(expressionStatementNode)
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    consumer.invoke(variableDeclarationNode)
    variableDeclarationNode.expression.accept(this)
  }

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    consumer.invoke(truthyVariableDeclarationNode)
    truthyVariableDeclarationNode.expression.accept(this)
  }

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) {
    consumer.invoke(multiVariableDeclarationNode)
    multiVariableDeclarationNode.expression.accept(this)
  }

  override fun visit(returnNode: ReturnNode) {
    consumer.invoke(returnNode)
    returnNode.expression?.accept(this)
  }

  override fun visit(voidExpression: VoidExpression) {
    consumer.invoke(voidExpression)
  }

  override fun visit(stringNode: StringNode) {
    consumer.invoke(stringNode)
    stringNode.parts.forEach { it.accept(this) }
  }

  override fun visit(stringConstantNode: StringConstantNode) = consumer.invoke(stringConstantNode)

  override fun visit(asNode: AsNode) {
    consumer.invoke(asNode)
    asNode.expressionNode.accept(this)
  }

  override fun visit(toStringNode: ToStringNode) {
    consumer.invoke(toStringNode)
    toStringNode.expressionNode.accept(this)
  }

  override fun visit(accessOperator: InvokeAccessOperator) = visitBinaryOperator(accessOperator)

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) = visitBinaryOperator(getFieldAccessOperator)

  override fun visit(booleanConstantNode: BooleanConstantNode) = consumer.invoke(booleanConstantNode)

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) = visitBinaryOperator(comparisonOperatorNode)

  override fun visit(andOperator: AndOperator) = visitBinaryOperator(andOperator)

  override fun visit(orOperator: OrOperator) = visitBinaryOperator(orOperator)

  override fun visit(notNode: NotNode) = visitUnaryOperator(notNode)

  override fun visit(ifStatementNode: IfStatementNode) {
    consumer.invoke(ifStatementNode)
    ifStatementNode.trueStatementNode.accept(this)
    ifStatementNode.falseStatementNode?.accept(this)
  }

  override fun visit(forStatement: ForStatement) {
    consumer.invoke(forStatement)
    forStatement.initStatement.accept(this)
    forStatement.iteratorStatement.accept(this)
    forStatement.endCondition.accept(this)
    forStatement.body.accept(this)
  }

  override fun visit(forInStatement: ForInStatement) {
    consumer.invoke(forInStatement)
    forInStatement.inExpression.accept(this)
    forInStatement.body.accept(this)
  }

  override fun visit(whileStatement: WhileStatement) {
    consumer.invoke(whileStatement)
    whileStatement.condition.accept(this)
    whileStatement.body.accept(this)
  }

  override fun visit(booleanExpression: BooleanExpressionNode) {
    consumer.invoke(booleanExpression)
    booleanExpression.innerExpression.accept(this)
  }

  override fun visit(nullValueNode: NullValueNode) = consumer.invoke(nullValueNode)

  override fun visit(incrNode: IncrNode) {
    consumer.invoke(incrNode)
    incrNode.variableReference.accept(this)
  }

  override fun visit(breakLoopNode: BreakLoopNode) = consumer.invoke(breakLoopNode)

  override fun visit(continueLoopNode: ContinueLoopNode) = consumer.invoke(continueLoopNode)

  override fun visit(rangeNode: RangeNode) {
    consumer.invoke(rangeNode)
    rangeNode.from.accept(this)
    rangeNode.to.accept(this)
  }

  override fun visit(literalListNode: LiteralArrayNode) {
    consumer.invoke(literalListNode)
    literalListNode.elements.forEach { it.accept(this) }
  }

  override fun visit(literalMapNode: LiteralMapNode) {
    consumer.invoke(literalMapNode)
    literalMapNode.entries.forEach {
      it.first.accept(this)
      it.second.accept(this)
    }
  }

  override fun visit(switchBranch: SwitchBranchNode) {
    consumer.invoke(switchBranch)
    switchBranch.conditionExpressionNode.accept(this)
    switchBranch.statementNode.accept(this)
  }

  override fun visit(switchNode: SwitchNode) {
    consumer.invoke(switchNode)
    switchNode.expressionNode.accept(this)
    switchNode.branches.forEach { it.accept(this) }
  }

  override fun visit(whenNode: WhenNode) {
    consumer.invoke(whenNode)
    whenNode.branches.forEach { it.accept(this) }
  }

  override fun visit(whenBranchNode: WhenBranchNode) {
    consumer.invoke(whenBranchNode)
    whenBranchNode.conditionExpressionNode.accept(this)
    whenBranchNode.statementNode.accept(this)
  }

  override fun visit(isOperator: IsOperator) = visitBinaryOperator(isOperator)
  override fun visit(isNotOperator: IsNotOperator) = visitBinaryOperator(isNotOperator)

  override fun visit(shortConstantNode: ShortConstantNode) = consumer.invoke(shortConstantNode)

  override fun visit(byteConstantNode: ByteConstantNode) = consumer.invoke(byteConstantNode)

  override fun visit(thisReference: ThisReference) = consumer.invoke(thisReference)

  override fun visit(superReference: SuperReference) = consumer.invoke(superReference)

  override fun visit(patternValueNode: LiteralPatternNode) = consumer.invoke(patternValueNode)
}