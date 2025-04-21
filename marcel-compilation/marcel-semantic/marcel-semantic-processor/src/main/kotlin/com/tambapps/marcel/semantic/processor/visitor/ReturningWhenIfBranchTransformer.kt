package com.tambapps.marcel.semantic.processor.visitor

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Valid and transform if necessary when/switch node branches to make them return a value
 */
class ReturningWhenIfBranchTransformer(
  node: CstNode,
  nodeTransformer: ((ExpressionNode) -> ExpressionNode)? = null
) :
  ReturningBranchTransformer(node, nodeTransformer) {


  override fun visit(node: ReturnStatementNode): StatementNode {
    throw MarcelSemanticException(node.token, "Cannot return in when/switch expressions")
  }

  override fun visit(node: ExpressionStatementNode): StatementNode {
    if (node.expressionNode.type == JavaType.void)
      throw MarcelSemanticException(node.token, "Expected a non void expression")
    return super.visit(node)
  }
}