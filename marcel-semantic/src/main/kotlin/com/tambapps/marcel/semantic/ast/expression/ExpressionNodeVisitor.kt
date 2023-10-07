package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode

interface ExpressionNodeVisitor<T> {

  fun visit(node: FunctionCallNode): T
  fun visit(node: ReferenceNode): T
  fun visit(node: ClassReferenceNode): T

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