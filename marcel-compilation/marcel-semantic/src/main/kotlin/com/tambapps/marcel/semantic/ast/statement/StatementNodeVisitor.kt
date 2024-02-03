package com.tambapps.marcel.semantic.ast.statement

interface StatementNodeVisitor<T> {

  fun visit(node: ExpressionStatementNode): T
  fun visit(node: ReturnStatementNode): T
  fun visit(node: BlockStatementNode): T
  fun visit(node: IfStatementNode): T
  fun visit(node: ForInIteratorStatementNode): T
  fun visit(node: WhileNode): T
  fun visit(node: DoWhileNode): T
  fun visit(node: ForStatementNode): T
  fun visit(node: BreakNode): T
  fun visit(node: ContinueNode): T
  fun visit(node: ThrowNode): T
  fun visit(node: TryNode): T

}