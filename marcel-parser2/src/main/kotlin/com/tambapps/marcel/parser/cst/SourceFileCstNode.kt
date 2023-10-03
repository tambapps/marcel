package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

// only handle scripts for now
class SourceFileCstNode(tokenStart: LexToken, tokenEnd: LexToken, val instructions: MutableList<CstNode>):
    CstNode(null, tokenStart, tokenEnd) {
    constructor(tokenStart: LexToken, tokenEnd: LexToken): this(tokenStart, tokenEnd, mutableListOf())
}