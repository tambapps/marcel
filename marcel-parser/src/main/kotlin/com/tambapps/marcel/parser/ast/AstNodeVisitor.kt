package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode

interface AstNodeVisitor {

  fun visit(integer: IntConstantNode)
  fun visit(operator: MulOperator)
  fun visit(operator: TernaryNode)
  fun visit(fCall: FunctionCallNode)
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