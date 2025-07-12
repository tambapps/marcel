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

  override fun isEqualTo(node: CstNode): Boolean {
    if (node !is StaticImportCstNode) return false

    if (className != node.className) return false
    if (memberName != node.memberName) return false

    return true
  }
}