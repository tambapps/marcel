package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode

interface AstNodeVisitor {

  fun visit(integer: IntConstantNode)
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
  fun visit(variableReferenceExpression: VariableReferenceExpression)
  fun visit(unaryMinus: UnaryMinus)
  fun visit(unaryPlus: UnaryPlus)
  fun visit(blockNode: BlockNode)
  fun visit(blockNode: FunctionBlockNode)

  fun visit(expressionStatementNode: ExpressionStatementNode)
  fun visit(variableDeclarationNode: VariableDeclarationNode)
  fun visit(returnNode: ReturnNode)
  fun visit(voidExpression: VoidExpression)
}