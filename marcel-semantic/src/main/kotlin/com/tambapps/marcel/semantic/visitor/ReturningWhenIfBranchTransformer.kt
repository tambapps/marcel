package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryCatchNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaType


/**
 * Valid and transform if necessary when/switch node branches to make them return a value
 */
class ReturningWhenIfBranchTransformer(val node: WhenCstNode): StatementNodeVisitor<StatementNode> {
  val collectedTypes = mutableListOf<JavaType>()

  override fun visit(node: ExpressionStatementNode): StatementNode {
    val expressionType = node.expressionNode.type
    if (expressionType == JavaType.void) throw MarcelSemanticException(node.token, "Expected a non void expression")
    else if (expressionType !== JavaType.Anything) collectedTypes.add(expressionType)
    return ReturnStatementNode(node.expressionNode, node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: ReturnStatementNode): StatementNode {
    throw MarcelSemanticException(node.token, "Cannot return in when/switch expressions")
  }

  override fun visit(node: BlockStatementNode): StatementNode {
    if (node.statements.isNotEmpty()) {
      val lastStatement = node.statements.last()
      node.statements[node.statements.lastIndex] = lastStatement.accept(this)
    }
    return node
  }

  override fun visit(node: IfStatementNode) = invalidStatement("if")

  override fun visit(node: ForInIteratorStatementNode) = invalidStatement("for loop")

  override fun visit(node: WhileNode) = invalidStatement("while loop")

  override fun visit(node: ForStatementNode) = invalidStatement("for loop")

  override fun visit(node: BreakNode) = invalidStatement("break")

  override fun visit(node: ContinueNode) = invalidStatement("continue")

  override fun visit(node: ThrowNode) = node

  override fun visit(node: TryCatchNode) = invalidStatement("try/catch")

  private fun invalidStatement(stmtName: String): StatementNode {
    throw MarcelSemanticException(node.token, "Cannot have a $stmtName as last statement of a when/switch branch")
  }
}