package com.tambapps.marcel.semantic.ast.statement

interface StatementNodeVisitor<T> {

  fun visit(node: ExpressionStatementNode): T

}