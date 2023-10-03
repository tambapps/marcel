package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

class DirectFieldReferenceCstNode(
  parent: CstNode?,
  override val value: String,
  token: LexToken
) : AbstractCstNode(parent, token) {


    override fun toString() = "@$value"

}