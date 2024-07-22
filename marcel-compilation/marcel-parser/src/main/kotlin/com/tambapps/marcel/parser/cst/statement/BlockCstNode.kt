package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class BlockCstNode(val statements: List<StatementCstNode>, parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as BlockCstNode

    return statements == other.statements
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + statements.hashCode()
    return result
  }

  override fun toString(): String {
    return statements.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}", transform = {"  $it" })
  }
}