package com.tambapps.marcel.parser.cst.statement

interface StatementCstNodeVisitor<T> {

  fun visit(node: ExpressionStatementCstNode): T

}