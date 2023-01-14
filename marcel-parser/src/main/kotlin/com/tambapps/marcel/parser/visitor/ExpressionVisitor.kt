package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.FunctionCallNode
import com.tambapps.marcel.parser.ast.IntConstantNode
import com.tambapps.marcel.parser.ast.TernaryNode
import com.tambapps.marcel.parser.ast.operator.binary.*

public interface ExpressionVisitor {

  fun visit(integer: IntConstantNode)
  fun visit(mulOperator: MulOperator)
  fun visit(ternaryNode: TernaryNode)
  fun visit(functionCallNode: FunctionCallNode)
  fun visit(divOperator: DivOperator)
  fun visit(plusOperator: PlusOperator)
  fun visit(minusOperator: MinusOperator)
  fun visit(powOperator: PowOperator)

}