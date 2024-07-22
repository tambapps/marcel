package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode

interface StatementCstNodeVisitor<T> {

  fun visit(node: ExpressionStatementCstNode): T
  fun visit(node: ReturnCstNode): T
  fun visit(node: VariableDeclarationCstNode): T
  fun visit(node: MultiVarDeclarationCstNode): T
  fun visit(node: IfStatementCstNode): T
  fun visit(node: ForInCstNode): T
  fun visit(node: ForInMultiVarCstNode): T
  fun visit(node: ForVarCstNode): T
  fun visit(node: WhileCstNode): T
  fun visit(node: DoWhileStatementCstNode): T
  fun visit(node: BlockCstNode): T
  fun visit(node: BreakCstNode): T
  fun visit(node: ContinueCstNode): T
  fun visit(node: ThrowCstNode): T
  fun visit(node: TryCatchCstNode): T

}