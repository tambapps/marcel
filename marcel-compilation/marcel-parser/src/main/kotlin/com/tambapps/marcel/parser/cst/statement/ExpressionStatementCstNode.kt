package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class ExpressionStatementCstNode(parent: CstNode? = null,
                                 val expressionNode: ExpressionCstNode,
                                 tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {

    constructor(expressionNode: ExpressionCstNode): this(expressionNode.parent, expressionNode, expressionNode.tokenStart,
      expressionNode.tokenEnd)
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)



  override fun toString() = "$expressionNode;"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ExpressionStatementCstNode) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }
}