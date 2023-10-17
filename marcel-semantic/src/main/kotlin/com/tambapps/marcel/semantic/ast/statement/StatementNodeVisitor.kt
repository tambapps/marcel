package com.tambapps.marcel.semantic.ast.statement

interface StatementNodeVisitor<T> {

  fun visit(node: ExpressionStatementNode): T
  fun visit(node: ReturnStatementNode): T
  fun visit(node: BlockStatementNode): T
  fun visit(node: IfStatementNode): T
  fun visit(node: ForInIteratorStatementNode): T

}