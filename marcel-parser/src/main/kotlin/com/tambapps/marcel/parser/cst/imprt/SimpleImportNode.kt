package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class SimpleImportNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
  val asName: String?
) :
  AbstractImportNode(parent, tokenStart, tokenEnd) {

    override fun <T> accept(visitor: ImportCstVisitor<T>) = visitor.visit(this)
}