package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class TypeCstNode(
    parent: CstNode?,
    override val value: String, // the type
    val genericTypes: List<String>,
    val arrayDimensions: Int,
    tokenStart: LexToken,
    tokenEnd: LexToken
) : CstNode(parent, tokenStart, tokenEnd) {
}