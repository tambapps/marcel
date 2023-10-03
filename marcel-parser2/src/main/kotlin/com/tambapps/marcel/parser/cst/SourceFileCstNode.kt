package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

// only handle scripts for now
class SourceFileCstNode(tokenStart: LexToken, tokenEnd: LexToken, val instructions: MutableList<StatementCstNode>):
    AbstractCstNode(null, tokenStart, tokenEnd) {
    constructor(tokenStart: LexToken, tokenEnd: LexToken): this(tokenStart, tokenEnd, mutableListOf())
}