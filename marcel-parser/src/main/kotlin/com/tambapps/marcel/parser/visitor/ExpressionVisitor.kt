package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.operator.binary.*

interface ExpressionVisitor {

  fun visit(integer: IntConstantNode)
  fun visit(operator: MulOperator)
  fun visit(operator: TernaryNode)
  fun visit(operator: FunctionCallNode)
  fun visit(operator: DivOperator)
  fun visit(operator: PlusOperator)
  fun visit(operator: MinusOperator)
  fun visit(operator: PowOperator)

}