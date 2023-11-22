package com.tambapps.marcel.repl

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.ModuleNode

data class ParserResult(val tokens: List<LexToken>, val classes: List<ClassNode>,
                        val imports: List<ImportNode>,
                        val dumbbells: Collection<String>,
                        val textHashCode: Int) {
    val scriptNode = classes.find { it.isScript }
}