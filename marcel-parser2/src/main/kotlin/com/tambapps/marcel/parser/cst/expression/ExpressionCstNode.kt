package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.CstNode

interface ExpressionCstNode: CstNode {

  fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U? = null): T

}