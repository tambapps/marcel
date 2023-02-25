package com.tambapps.marcel.repl

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.ClassNode

data class ParserResult(val tokens: List<LexToken>, val classes: List<ClassNode>, val textHashCode: Int) {
    val scriptNode = classes.find { it.isScript }!!
}