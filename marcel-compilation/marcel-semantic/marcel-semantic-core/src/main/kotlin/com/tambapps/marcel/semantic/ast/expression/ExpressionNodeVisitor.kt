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
import com.tambapps.marcel.semantic.ast.expression.literal.NewArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.OrNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode

interface ExpressionNodeVisitor<T> {

  fun visit(node: NotNode): T
  fun visit(node: IncrNode): T
  fun visit(node: DupNode): T
  fun visit(node: PopNode): T

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
  fun visit(node: AndNode): T
  fun visit(node: OrNode): T
  fun visit(node: GeNode): T
  fun visit(node: GtNode): T
  fun visit(node: LeNode): T
  fun visit(node: LtNode): T
  fun visit(node: ElvisNode): T

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
  fun visit(node: InstanceOfNode): T
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
  fun visit(node: NewArrayNode): T
  fun visit(node: MapNode): T
  fun visit(node: ExprErrorNode): T

}