package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class ThrowNode(node: CstNode, val expressionNode: ExpressionNode) : AbstractStatementNode(node) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

}