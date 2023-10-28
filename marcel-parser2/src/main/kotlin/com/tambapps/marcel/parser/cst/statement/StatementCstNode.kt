package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.parser.cst.CstNode

interface StatementCstNode: CstNode {
  fun <T> accept(visitor: StatementCstNodeVisitor<T>): T

}