package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class WildcardImportNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val prefix: String
) :
  AbstractImportNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ImportCstVisitor<T>) = visitor.visit(this)
}