package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode

interface ExpressionNodeVisitor<T> {

  fun visit(node: NotNode): T

  fun visit(node: VariableAssignmentNode): T
  fun visit(node: ArrayIndexAssignmentNode): T
  fun visit(node: LeftShiftNode): T
  fun visit(node: RightShiftNode): T
  fun visit(node: DivNode): T
  fun visit(node: MinusNode): T
  fun visit(node: ModNode): T
  fun visit(node: MulNode): T
  fun visit(node: PlusNode): T
  fun visit(node: IsEqualNode): T
  fun visit(node: IsNotEqualNode): T

  fun visit(node: TernaryNode): T

  fun visit(node: ArrayAccessNode): T
  fun visit(node: FunctionCallNode): T
  fun visit(node: NewInstanceNode): T
  fun visit(node: ThisConstructorCallNode): T
  fun visit(node: SuperConstructorCallNode): T
  fun visit(node: ReferenceNode): T
  fun visit(node: ClassReferenceNode): T
  fun visit(node: ThisReferenceNode): T
  fun visit(node: SuperReferenceNode): T

  fun visit(node: JavaCastNode): T
  fun visit(node: BoolConstantNode): T
  fun visit(node: ByteConstantNode): T
  fun visit(node: CharConstantNode): T
  fun visit(node: StringConstantNode): T
  fun visit(node: StringNode): T
  fun visit(node: DoubleConstantNode): T
  fun visit(node: FloatConstantNode): T
  fun visit(node: IntConstantNode): T
  fun visit(node: LongConstantNode): T
  fun visit(node: NullValueNode): T
  fun visit(node: ShortConstantNode): T
  fun visit(node: VoidExpressionNode): T
  fun visit(node: ArrayNode): T
  fun visit(node: MapNode): T

}