package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.ast.Ast2Node

interface StatementNode: Ast2Node {

  fun <T> accept(visitor: StatementNodeVisitor<T>): T

}