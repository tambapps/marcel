package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.FunctionCallNode
import com.tambapps.marcel.parser.ast.IntConstantNode
import com.tambapps.marcel.parser.ast.TernaryNode
import com.tambapps.marcel.parser.ast.operator.binary.*

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