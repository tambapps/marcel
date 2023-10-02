package com.tambapps.marcel.parser.cst.expression.literral

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class DoubleCstNode(parent: CstNode? = null, override val value: Double, token: LexToken) : CstNode(parent, token) {

    override fun toString() = "${value}d"

}