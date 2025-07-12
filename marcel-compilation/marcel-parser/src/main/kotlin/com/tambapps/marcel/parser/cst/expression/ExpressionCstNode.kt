package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.IdentifiableCstNode
import com.tambapps.marcel.parser.cst.IsEqualVisitor
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.visitor.ForEachNodeVisitor

/**
 * CST node of a Marcel expression
 */
interface ExpressionCstNode: IdentifiableCstNode {

  fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U? = null): T

  fun forEach(consume: (CstNode) -> Unit) = accept(ForEachNodeVisitor(consume))

  override fun isEqualTo(node: CstNode): Boolean = accept(IsEqualVisitor(node))
}