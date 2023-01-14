package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.operator.binary.*
import com.tambapps.marcel.parser.ast.expression.operator.unary.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.operator.unary.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.variable.VariableReferenceExpression
import com.tambapps.marcel.parser.ast.statement.variable.VariableAssignmentNode

interface ExpressionVisitor {

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

}