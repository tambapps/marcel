package com.tambapps.marcel.semantic.ast.expression

interface ExpressionNodeVisitor<T> {

  fun visit(node: BoolConstantNode): T
  fun visit(node: ByteConstantNode): T
  fun visit(node: CharConstantNode): T
  fun visit(node: DoubleConstantNode): T
  fun visit(node: FloatConstantNode): T
  fun visit(node: IntConstantNode): T
  fun visit(node: LongConstantNode): T
  fun visit(node: NullValueNode): T
  fun visit(node: ShortConstantNode): T
  fun visit(node: VoidExpressionNode): T

}