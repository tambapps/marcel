package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class StaticImportCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
  // TODO rename memberName
  val methodName: String,

  ) :
  AbstractImportCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ImportCstVisitor<T>) = visitor.visit(this)
}