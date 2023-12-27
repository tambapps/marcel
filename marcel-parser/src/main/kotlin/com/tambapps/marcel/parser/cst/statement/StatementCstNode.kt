package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ForEachNodeVisitor

/**
 * CST node of a Marcel statement
 *
 */
interface StatementCstNode: CstNode {
  fun <T> accept(visitor: StatementCstNodeVisitor<T>): T

  fun forEach(consume: (CstNode) -> Unit) = accept(ForEachNodeVisitor(consume))

}