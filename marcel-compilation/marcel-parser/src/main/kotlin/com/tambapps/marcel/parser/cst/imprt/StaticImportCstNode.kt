package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstVisitor

class StaticImportCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
  val memberName: String,

  ) :
  AbstractImportCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ImportCstVisitor<T>) = visitor.visit(this)
}