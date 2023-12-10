package com.tambapps.marcel.repl

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ImportNode

data class SemanticResult(val tokens: List<LexToken>,
                          val cst: SourceFileNode,
                          val classes: List<ClassNode>,
                          val imports: List<ImportNode>,
                          val textHashCode: Int) {
  val scriptNode = classes.find { it.isScript }
  val dumbbells get() = cst.dumbbells
  val runMethodNode = scriptNode?.methods?.find { it.name == "run" && it.parameters.size == 1 }
}