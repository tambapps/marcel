package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.imprt.ImportNode

// only handle scripts for now
class SourceFileNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val packageName: String?, // without extension
  val dumbbells: List<String>
  ): AbstractCstNode(null, tokenStart, tokenEnd) {

  var script: ScriptCstNode? = null

  val classes = mutableListOf<ClassCstNode>()
  val imports = mutableListOf<ImportNode>()
  val extensionImports = mutableListOf<TypeNode>()

}