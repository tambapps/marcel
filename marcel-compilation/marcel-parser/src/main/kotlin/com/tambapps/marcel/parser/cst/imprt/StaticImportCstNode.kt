package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstNodeVisitor

class StaticImportCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
  val memberName: String,

  ) :
  AbstractImportCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: ImportCstNodeVisitor<T>) = visitor.visit(this)

  override fun isSyntaxEqualTo(other: CstNode): Boolean {
    if (other !is StaticImportCstNode) return false

    if (className != other.className) return false
    if (memberName != other.memberName) return false

    return true
  }
}