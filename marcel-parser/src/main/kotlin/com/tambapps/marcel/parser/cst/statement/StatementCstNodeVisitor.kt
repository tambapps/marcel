package com.tambapps.marcel.parser.cst.statement

interface StatementCstNodeVisitor<T> {

  fun visit(node: ExpressionStatementCstNode): T
  fun visit(node: ReturnCstNode): T
  fun visit(node: VariableDeclarationCstNode): T
  fun visit(node: MultiVarDeclarationCstNode): T
  fun visit(node: IfCstStatementNode): T
  fun visit(node: ForInCstNode): T
  fun visit(node: ForVarCstNode): T
  fun visit(node: WhileCstNode): T
  fun visit(node: BlockCstNode): T
  fun visit(node: BreakCstNode): T
  fun visit(node: ContinueCstNode): T
  fun visit(node: ThrowCstNode): T
  fun visit(node: TryCatchCstNode): T

}