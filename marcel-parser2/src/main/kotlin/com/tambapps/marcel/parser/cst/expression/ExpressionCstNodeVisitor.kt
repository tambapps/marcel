package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode

interface ExpressionCstNodeVisitor<T> {

  fun visit(node: DoubleCstNode): T
  fun visit(node: FloatCstNode): T
  fun visit(node: IntCstNode): T
  fun visit(node: LongCstNode): T
  fun visit(node: NullCstNode): T
  fun visit(node: StringCstNode): T
  fun visit(node: TemplateStringNode): T
  fun visit(node: MapCstNode): T
  fun visit(node: ArrayCstNode): T

  fun visit(node: BinaryOperatorCstNode): T
  fun visit(node: VariableAssignmentCstNode): T

  fun visit(node: ClassReferenceCstNode): T
  fun visit(node: ThisReferenceCstNode): T
  fun visit(node: SuperReferenceCstNode): T
  fun visit(node: DirectFieldReferenceCstNode): T
  fun visit(node: IncrCstNode): T
  fun visit(node: IndexAccessCstNode): T
  fun visit(node: ReferenceCstNode): T
  fun visit(node: FunctionCallCstNode): T
  fun visit(node: SuperConstructorCallCstNode): T
  fun visit(node: NewInstanceCstNode): T

}