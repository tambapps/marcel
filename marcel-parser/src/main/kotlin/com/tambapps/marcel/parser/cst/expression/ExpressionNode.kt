package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ForEachNodeVisitor

interface ExpressionNode: CstNode {

  fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U? = null): T

  fun forEach(consume: (CstNode) -> Unit) = accept(ForEachNodeVisitor(consume))

}