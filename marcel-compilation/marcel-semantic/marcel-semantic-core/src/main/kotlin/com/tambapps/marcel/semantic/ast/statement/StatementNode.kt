package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.ast.AstNode

interface StatementNode: AstNode {

  fun <T> accept(visitor: StatementNodeVisitor<T>): T

}