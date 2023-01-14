package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.FunctionCallNode
import com.tambapps.marcel.parser.ast.IntConstantNode
import com.tambapps.marcel.parser.ast.TernaryNode

interface ExpressionVisitor {

  fun visit(integer: IntConstantNode)
  fun visit(binaryOperatorNode: BinaryOperatorNode)
  fun visit(ternaryNode: TernaryNode)
  fun visit(functionCallNode: FunctionCallNode)

}