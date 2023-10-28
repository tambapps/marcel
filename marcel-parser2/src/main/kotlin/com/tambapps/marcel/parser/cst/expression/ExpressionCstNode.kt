package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.CstNode

interface ExpressionCstNode: CstNode {

  fun <T> accept(visitor: ExpressionCstNodeVisitor<T>): T
}