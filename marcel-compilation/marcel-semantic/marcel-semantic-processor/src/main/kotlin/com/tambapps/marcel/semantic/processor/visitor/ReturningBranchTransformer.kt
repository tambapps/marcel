package com.tambapps.marcel.semantic.processor.visitor

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.symbol.type.JavaType


/**
 * Valid and transform if necessary when/switch node branches to make them return a value
 */
open class ReturningBranchTransformer(
  private val node: CstNode,
  // useful to cast
  private val nodeTransformer: ((ExpressionNode) -> ExpressionNode)? = null
) : StatementNodeVisitor<StatementNode> {
  val collectedTypes = mutableListOf<JavaType>()

  override fun visit(node: ExpressionStatementNode): StatementNode {
    val expressionType = node.expressionNode.type
    if (expressionType == JavaType.void) {
      return BlockStatementNode(
        mutableListOf(
          node,
          ReturnStatementNode(
            nodeTransformer?.invoke(NullValueNode(node.token)) ?: NullValueNode(node.token),
            node.tokenStart,
            node.tokenEnd
          )
        ), node.tokenStart, node.tokenEnd
      )
    } else if (expressionType !== JavaType.Anything) collectedTypes.add(expressionType)
    return ReturnStatementNode(
      nodeTransformer?.invoke(node.expressionNode) ?: node.expressionNode,
      node.tokenStart,
      node.tokenEnd
    )
  }

  override fun visit(node: ReturnStatementNode): StatementNode {
    return ReturnStatementNode(
      nodeTransformer?.invoke(node.expressionNode) ?: node.expressionNode,
      node.tokenStart, node.tokenEnd
    )
  }

  override fun visit(node: BlockStatementNode): StatementNode {
    if (node.statements.isNotEmpty()) {
      val lastStatement = node.statements.last()
      node.statements[node.statements.lastIndex] = lastStatement.accept(this)
    }
    return node
  }

  override fun visit(node: IfStatementNode) = stmtThenReturnNull(node)

  override fun visit(node: ForInIteratorStatementNode) = stmtThenReturnNull(node)

  override fun visit(node: WhileNode) = stmtThenReturnNull(node)
  override fun visit(node: DoWhileNode) = stmtThenReturnNull(node)

  override fun visit(node: ForStatementNode) = stmtThenReturnNull(node)

  override fun visit(node: BreakNode) = invalidStatement("break")

  override fun visit(node: ContinueNode) = invalidStatement("continue")

  override fun visit(node: ThrowNode) = node

  override fun visit(node: TryNode) = stmtThenReturnNull(node)

  private fun stmtThenReturnNull(node: StatementNode) = BlockStatementNode(mutableListOf(node, ReturnStatementNode(NullValueNode(node.token))))

  private fun invalidStatement(stmtName: String): StatementNode {
    throw MarcelSemanticException(node.token, "Cannot have a $stmtName as last statement of a when/switch branch")
  }
}