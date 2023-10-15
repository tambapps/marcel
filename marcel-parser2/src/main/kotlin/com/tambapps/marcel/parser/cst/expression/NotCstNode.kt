package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class NotCstNode(val expression: CstExpressionNode, parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)
}