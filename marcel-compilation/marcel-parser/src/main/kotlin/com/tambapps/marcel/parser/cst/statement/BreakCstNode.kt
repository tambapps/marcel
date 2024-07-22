package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class BreakCstNode(parent: CstNode?, token: LexToken) : AbstractStatementCstNode(parent, token) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
}