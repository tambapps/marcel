package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstNodeVisitor

class WildcardImportCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val prefix: String
) :
  AbstractImportCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ImportCstNodeVisitor<T>) = visitor.visit(this)

  override fun isSyntaxEqualTo(other: CstNode): Boolean {
    if (other !is WildcardImportCstNode) return false
    return prefix == other.prefix
  }
}