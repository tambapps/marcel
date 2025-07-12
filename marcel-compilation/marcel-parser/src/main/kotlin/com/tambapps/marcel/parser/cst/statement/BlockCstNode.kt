package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class BlockCstNode(val statements: List<StatementCstNode>, parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)


  override fun toString(): String {
    return statements.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}", transform = {"  $it" })
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlockCstNode) return false

    if (statements != other.statements) return false

    return true
  }

  override fun hashCode(): Int {
    return statements.hashCode()
  }
}