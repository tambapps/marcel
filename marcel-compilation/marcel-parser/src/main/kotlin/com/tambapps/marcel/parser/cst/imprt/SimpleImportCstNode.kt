package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstNodeVisitor

class SimpleImportCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
  val asName: String?
) :
  AbstractImportCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ImportCstNodeVisitor<T>) = visitor.visit(this)

  override fun isEqualTo(other: CstNode): Boolean {
    if (other !is SimpleImportCstNode) return false

    if (className != other.className) return false
    if (asName != other.asName) return false

    return true
  }
}