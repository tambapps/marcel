package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.expression.AllInCstNode
import com.tambapps.marcel.parser.cst.expression.AnyInCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.FindInCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.MapFilterCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode

/**
 * Visitor of a [CstNode][com.tambapps.marcel.parser.cst.CstNode]
 *
 *  smartCastType corresponds to a type that can be used to better handle the node, but it is not necessarily used
 *
 * @param T the return type of the visitor
 * @param U the smart cast type
 */
interface ExpressionCstNodeVisitor<T, U> {

  fun visit(node: BoolCstNode, smartCastType: U? = null): T
  fun visit(node: DoubleCstNode, smartCastType: U? = null): T
  fun visit(node: FloatCstNode, smartCastType: U? = null): T
  fun visit(node: IntCstNode, smartCastType: U? = null): T
  fun visit(node: LongCstNode, smartCastType: U? = null): T
  fun visit(node: NullCstNode, smartCastType: U? = null): T
  fun visit(node: StringCstNode, smartCastType: U? = null): T
  fun visit(node: RegexCstNode, smartCastType: U? = null): T
  fun visit(node: CharCstNode, smartCastType: U? = null): T
  fun visit(node: TemplateStringCstNode, smartCastType: U? = null): T
  fun visit(node: MapCstNode, smartCastType: U? = null): T
  fun visit(node: ArrayCstNode, smartCastType: U? = null): T
  fun visit(node: MapFilterCstNode, smartCastType: U? = null): T
  fun visit(node: AllInCstNode, smartCastType: U? = null): T
  fun visit(node: AnyInCstNode, smartCastType: U? = null): T
  fun visit(node: FindInCstNode, smartCastType: U? = null): T

  fun visit(node: UnaryMinusCstNode, smartCastType: U? = null): T
  fun visit(node: NotCstNode, smartCastType: U? = null): T
  fun visit(node: WrappedExpressionCstNode, smartCastType: U? = null): T

  fun visit(node: BinaryOperatorCstNode, smartCastType: U? = null): T
  fun visit(node: ElvisThrowCstNode, smartCastType: U? = null): T
  fun visit(node: BinaryTypeOperatorCstNode, smartCastType: U? = null): T
  fun visit(node: TernaryCstNode, smartCastType: U? = null): T
  fun visit(node: ClassReferenceCstNode, smartCastType: U? = null): T
  fun visit(node: ThisReferenceCstNode, smartCastType: U? = null): T
  fun visit(node: SuperReferenceCstNode, smartCastType: U? = null): T
  fun visit(node: DirectFieldReferenceCstNode, smartCastType: U? = null): T
  fun visit(node: IncrCstNode, smartCastType: U? = null): T
  fun visit(node: IndexAccessCstNode, smartCastType: U? = null): T
  fun visit(node: ReferenceCstNode, smartCastType: U? = null): T
  fun visit(node: FunctionCallCstNode, smartCastType: U? = null): T
  fun visit(node: SuperConstructorCallCstNode, smartCastType: U? = null): T
  fun visit(node: ThisConstructorCallCstNode, smartCastType: U? = null): T
  fun visit(node: NewInstanceCstNode, smartCastType: U? = null): T
  fun visit(node: WhenCstNode, smartCastType: U? = null): T
  fun visit(node: SwitchCstNode, smartCastType: U? = null): T
  fun visit(node: LambdaCstNode, smartCastType: U? = null): T
  fun visit(node: AsyncBlockCstNode, smartCastType: U? = null): T
  fun visit(node: TruthyVariableDeclarationCstNode, smartCastType: U? = null): T

}