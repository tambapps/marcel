package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.expression.literal.ArrayNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolNode
import com.tambapps.marcel.parser.cst.expression.literal.CharNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatNode
import com.tambapps.marcel.parser.cst.expression.literal.IntNode
import com.tambapps.marcel.parser.cst.expression.literal.LongNode
import com.tambapps.marcel.parser.cst.expression.literal.MapNode
import com.tambapps.marcel.parser.cst.expression.literal.NullNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexNode
import com.tambapps.marcel.parser.cst.expression.literal.StringNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceNode

/**
 * smartCastType corresponds to a type that can be used to better handle the node, but it is not necessarily used
 */
interface ExpressionCstNodeVisitor<T, U> {

  fun visit(node: BoolNode, smartCastType: U? = null): T
  fun visit(node: DoubleNode, smartCastType: U? = null): T
  fun visit(node: FloatNode, smartCastType: U? = null): T
  fun visit(node: IntNode, smartCastType: U? = null): T
  fun visit(node: LongNode, smartCastType: U? = null): T
  fun visit(node: NullNode, smartCastType: U? = null): T
  fun visit(node: StringNode, smartCastType: U? = null): T
  fun visit(node: RegexNode, smartCastType: U? = null): T
  fun visit(node: CharNode, smartCastType: U? = null): T
  fun visit(node: TemplateStringNode, smartCastType: U? = null): T
  fun visit(node: MapNode, smartCastType: U? = null): T
  fun visit(node: ArrayNode, smartCastType: U? = null): T

  fun visit(node: UnaryMinusNode, smartCastType: U? = null): T
  fun visit(node: NotNode, smartCastType: U? = null): T

  fun visit(node: BinaryOperatorNode, smartCastType: U? = null): T
  fun visit(node: BinaryTypeOperatorNode, smartCastType: U? = null): T
  fun visit(node: TernaryNode, smartCastType: U? = null): T
  fun visit(node: ClassReferenceNode, smartCastType: U? = null): T
  fun visit(node: ThisReferenceNode, smartCastType: U? = null): T
  fun visit(node: SuperReferenceNode, smartCastType: U? = null): T
  fun visit(node: DirectFieldReferenceNode, smartCastType: U? = null): T
  fun visit(node: IncrNode, smartCastType: U? = null): T
  fun visit(node: IndexAccessNode, smartCastType: U? = null): T
  fun visit(node: ReferenceNode, smartCastType: U? = null): T
  fun visit(node: FunctionCallNode, smartCastType: U? = null): T
  fun visit(node: SuperConstructorCallNode, smartCastType: U? = null): T
  fun visit(node: ThisConstructorCallNode, smartCastType: U? = null): T
  fun visit(node: NewInstanceNode, smartCastType: U? = null): T
  fun visit(node: WhenNode, smartCastType: U? = null): T
  fun visit(node: SwitchNode, smartCastType: U? = null): T
  fun visit(node: LambdaNode, smartCastType: U? = null): T
  fun visit(node: TruthyVariableDeclarationNode, smartCastType: U? = null): T

}