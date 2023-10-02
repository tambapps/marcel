package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class IncrCstNode(
    parent: CstNode?,
    override val value: String,
    val amount: Int,
    val returnValueBefore: Boolean,
    tokenStart: LexToken, tokenEnd: LexToken) : CstNode(parent, tokenStart, tokenEnd) {


}