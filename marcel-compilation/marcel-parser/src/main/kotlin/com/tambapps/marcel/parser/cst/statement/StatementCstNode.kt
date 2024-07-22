package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.AnyStatementCstNodeVisitor
import com.tambapps.marcel.parser.cst.visitor.ForEachNodeVisitor
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

/**
 * CST node of a Marcel statement
 *
 */
interface StatementCstNode: CstNode {
  fun <T> accept(visitor: StatementCstNodeVisitor<T>): T

  fun forEach(consume: (CstNode) -> Unit) = accept(ForEachNodeVisitor(consume))

  fun any(predicate: (StatementCstNode) -> Boolean) = accept(AnyStatementCstNodeVisitor(predicate))

}