package com.tambapps.marcel.parser.cst.statement

interface StatementCstNodeVisitor<T> {

  fun visit(node: ExpressionStatementNode): T
  fun visit(node: ReturnNode): T
  fun visit(node: VariableDeclarationNode): T
  fun visit(node: MultiVarDeclarationNode): T
  fun visit(node: IfStatementNode): T
  fun visit(node: ForInNode): T
  fun visit(node: ForInMultiVarNode): T
  fun visit(node: ForVarNode): T
  fun visit(node: WhileNode): T
  fun visit(node: BlockNode): T
  fun visit(node: BreakNode): T
  fun visit(node: ContinueNode): T
  fun visit(node: ThrowNode): T
  fun visit(node: TryCatchNode): T

}