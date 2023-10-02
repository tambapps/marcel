package com.tambapps.marcel.parser.cst.expression.literral

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class LongCstNode(parent: CstNode? = null, override val value: Long, token: LexToken) : CstNode(parent, token) {

    override fun toString() = "${value}L"

}