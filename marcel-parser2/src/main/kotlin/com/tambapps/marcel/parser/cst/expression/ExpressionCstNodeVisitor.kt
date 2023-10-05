package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode

interface ExpressionCstNodeVisitor<T> {

  fun visit(node: DoubleCstNode): T
  fun visit(node: FloatCstNode): T
  fun visit(node: IntCstNode): T
  fun visit(node: LongCstNode): T



  fun visit(node: ClassReferenceCstNode): T
  fun visit(node: DirectFieldReferenceCstNode): T
  fun visit(node: IncrCstNode): T
  fun visit(node: IndexAccessCstNode): T
  fun visit(node: ReferenceCstNode): T
  fun visit(node: FunctionCallCstNode): T

}