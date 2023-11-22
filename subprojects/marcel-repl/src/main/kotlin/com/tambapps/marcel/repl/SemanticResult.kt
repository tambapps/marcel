package com.tambapps.marcel.repl

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ImportNode

data class SemanticResult(val tokens: List<LexToken>,
                          val cst: SourceFileCstNode,
                          val classes: List<ClassNode>,
                          val imports: List<ImportNode>,
                          val textHashCode: Int) {
  val scriptNode = classes.find { it.isScript }
  val dumbbells get() = cst.dumbbells
}