package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

class ClassReferenceCstNode(
  parent: CstNode?,
  override val value: String,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString() = "$value.class"

}