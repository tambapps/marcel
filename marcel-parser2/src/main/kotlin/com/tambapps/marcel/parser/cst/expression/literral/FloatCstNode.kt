package com.tambapps.marcel.parser.cst.expression.literral

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

class FloatCstNode(parent: CstNode? = null, override val value: Float, token: LexToken) : AbstractCstNode(parent, token) {

    override fun toString() = "${value}f"

}