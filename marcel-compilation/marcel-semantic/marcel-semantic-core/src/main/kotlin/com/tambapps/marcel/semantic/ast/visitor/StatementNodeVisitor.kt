package com.tambapps.marcel.semantic.ast.visitor

import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode

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