package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class ThisReferenceNode(parent: CstNode?, token: LexToken): AbstractExpressionNode(parent, token) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = "this"

}