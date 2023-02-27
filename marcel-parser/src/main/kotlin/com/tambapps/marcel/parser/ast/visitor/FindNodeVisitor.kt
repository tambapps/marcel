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
import com.tambapps.marcel.parser.ast.expression.LiteralPatternNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NamedParametersConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.NotNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.OrOperator
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

class FindNodeVisitor(private val predicate: (AstNode) -> Boolean): AstNodeVisitor<AstNode?> {

  private fun node(astNode: AstNode): AstNode? {
    return if (predicate.invoke(astNode)) astNode else null
  }

  private fun binaryNode(operator: BinaryOperatorNode): AstNode? {
    return node(operator) ?: operator.leftOperand.accept(this) ?: operator.rightOperand.accept(this)
  }

  override fun visit(integer: IntConstantNode) = node(integer)

  override fun visit(longConstantNode: LongConstantNode) = node(longConstantNode)

  override fun visit(floatConstantNode: FloatConstantNode) = node(floatConstantNode)

  override fun visit(doubleConstantNode: DoubleConstantNode) = node(doubleConstantNode)

  override fun visit(charNode: CharConstantNode) = node(charNode)

  override fun visit(operator: MulOperator) = binaryNode(operator)

  override fun visit(operator: TernaryNode): AstNode? {
    return node(operator) ?: operator.boolExpression.accept(this)
    ?: operator.trueExpression.accept(this) ?: operator.falseExpression.accept(this)
  }

  override fun visit(elvisOperator: ElvisOperator) = binaryNode(elvisOperator)

  override fun visit(fCall: FunctionCallNode) = node(fCall) ?: fCall.arguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(fCall: ConstructorCallNode) = node(fCall) ?: fCall.arguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(fCall: NamedParametersConstructorCallNode) = node(fCall) ?: fCall.namedArguments.firstNotNullOfOrNull { it.valueExpression.accept(this) }

  override fun visit(fCall: SuperConstructorCallNode) = node(fCall) ?: fCall.arguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(operator: DivOperator) = binaryNode(operator)

  override fun visit(operator: PlusOperator) = binaryNode(operator)

  override fun visit(operator: MinusOperator) = binaryNode(operator)

  override fun visit(operator: PowOperator) = binaryNode(operator)

  override fun visit(rightShiftOperator: RightShiftOperator) = binaryNode(rightShiftOperator)

  override fun visit(leftShiftOperator: LeftShiftOperator) = binaryNode(leftShiftOperator)

  override fun visit(variableAssignmentNode: VariableAssignmentNode) = node(variableAssignmentNode) ?: variableAssignmentNode.expression.accept(this)

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) = node(fieldAssignmentNode) ?: fieldAssignmentNode.fieldNode.accept(this)
  ?: fieldAssignmentNode.expression.accept(this)

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) = node(indexedVariableAssignmentNode)
    ?: indexedVariableAssignmentNode.indexedReference.accept(this) ?: indexedVariableAssignmentNode.expression.accept(this)

  override fun visit(referenceExpression: ReferenceExpression) = node(referenceExpression)

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) = node(indexedReferenceExpression)
    ?: indexedReferenceExpression.indexArguments.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(unaryMinus: UnaryMinus) = node(unaryMinus) ?: unaryMinus.operand.accept(this)

  override fun visit(unaryPlus: UnaryPlus) = node(unaryPlus) ?: unaryPlus.operand.accept(this)

  override fun visit(blockNode: BlockNode) = node(blockNode) ?: blockNode.statements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(blockNode: FunctionBlockNode) = node(blockNode) ?: blockNode.statements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(lambdaNode: LambdaNode) = node(lambdaNode) ?: lambdaNode.blockNode.accept(this)

  override fun visit(expressionStatementNode: ExpressionStatementNode) = node(expressionStatementNode) ?: expressionStatementNode.expression.accept(this)

  override fun visit(variableDeclarationNode: VariableDeclarationNode) = node(variableDeclarationNode) ?: variableDeclarationNode.expression.accept(this)

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) = node(truthyVariableDeclarationNode)
    ?: truthyVariableDeclarationNode.expression.accept(this)

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) = node(multiVariableDeclarationNode)
    ?: multiVariableDeclarationNode.expression.accept(this)

  override fun visit(returnNode: ReturnNode) = node(returnNode) ?: returnNode.expression.accept(this)

  override fun visit(voidExpression: VoidExpression) = node(voidExpression)

  override fun visit(stringNode: StringNode) = node(stringNode) ?: stringNode.parts.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(stringConstantNode: StringConstantNode) = node(stringConstantNode)

  override fun visit(asNode: AsNode) = node(asNode) ?: asNode.expressionNode.accept(this)

  override fun visit(toStringNode: ToStringNode) = node(toStringNode) ?: toStringNode.expressionNode.accept(this)

  override fun visit(accessOperator: InvokeAccessOperator) = binaryNode(accessOperator)

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) = binaryNode(getFieldAccessOperator)

  override fun visit(booleanConstantNode: BooleanConstantNode) = node(booleanConstantNode)

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) = binaryNode(comparisonOperatorNode)

  override fun visit(andOperator: AndOperator) = binaryNode(andOperator)

  override fun visit(orOperator: OrOperator) = binaryNode(orOperator)

  override fun visit(notNode: NotNode) = node(notNode) ?: notNode.operand.accept(this)

  override fun visit(ifStatementNode: IfStatementNode) = node(ifStatementNode) ?: ifStatementNode.condition.accept(this)
  ?: ifStatementNode.trueStatementNode.accept(this) ?: ifStatementNode.falseStatementNode?.accept(this)

  override fun visit(forStatement: ForStatement) = node(forStatement) ?: forStatement.initStatement.accept(this)
  ?: forStatement.iteratorStatement.accept(this) ?: forStatement.endCondition.accept(this)

  override fun visit(tryCatchNode: TryCatchNode) = tryCatchNode.tryStatementNode.accept(this)
    ?: tryCatchNode.catchNodes.firstNotNullOfOrNull { it.statementNode.accept(this) }
    ?: tryCatchNode.finallyBlock?.accept(this)

  override fun visit(forInStatement: ForInStatement) = node(forInStatement) ?: forInStatement.inExpression.accept(this)

  override fun visit(whileStatement: WhileStatement) = node(whileStatement) ?: whileStatement.condition.accept(this)
  ?: whileStatement.body.accept(this)

  override fun visit(booleanExpression: BooleanExpressionNode) = node(booleanExpression) ?: booleanExpression.innerExpression.accept(this)

  override fun visit(nullValueNode: NullValueNode) = node(nullValueNode)

  override fun visit(incrNode: IncrNode) = node(incrNode) ?: incrNode.variableReference.accept(this)

  override fun visit(breakLoopNode: BreakLoopNode) = node(breakLoopNode)

  override fun visit(continueLoopNode: ContinueLoopNode) = node(continueLoopNode)

  override fun visit(rangeNode: RangeNode) = node(rangeNode) ?: rangeNode.from.accept(this) ?: rangeNode.to.accept(this)

  override fun visit(literalListNode: LiteralArrayNode) = node(literalListNode) ?: literalListNode.elements.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(literalMapNode: LiteralMapNode) = node(literalMapNode) ?:
  literalMapNode.entries.firstNotNullOfOrNull { it.first.accept(this) ?: it.second.accept(this) }

  override fun visit(switchBranch: SwitchBranchNode) = node(switchBranch) ?: switchBranch.conditionExpressionNode.accept(this)
  ?: switchBranch.statementNode.accept(this)

  override fun visit(switchNode: SwitchNode) = node(switchNode) ?: switchNode.expressionNode.accept(this)
  ?: switchNode.elseStatement?.accept(this)
  ?: switchNode.branches.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(whenBranchNode: WhenBranchNode)= node(whenBranchNode) ?: whenBranchNode.conditionExpressionNode.accept(this)
  ?: whenBranchNode.statementNode.accept(this)

  override fun visit(whenNode: WhenNode) = node(whenNode)
    ?: whenNode.elseStatement?.accept(this)
    ?: whenNode.branches.firstNotNullOfOrNull { it.accept(this) }

  override fun visit(isOperator: IsOperator) = binaryNode(isOperator)
  override fun visit(isNotOperator: IsNotOperator) = binaryNode(isNotOperator)

  override fun visit(byteConstantNode: ByteConstantNode) = node(byteConstantNode)

  override fun visit(shortConstantNode: ShortConstantNode) = node(shortConstantNode)

  override fun visit(thisReference: ThisReference) = node(thisReference)

  override fun visit(superReference: SuperReference) = node(superReference)

  override fun visit(patternValueNode: LiteralPatternNode) = node(patternValueNode)

  override fun visit(findOperator: FindOperator) = binaryNode(findOperator)
}