package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

// only handle scripts for now
class SourceFileCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val packageName: String?, // without extension
): AbstractCstNode(null, tokenStart, tokenEnd) {

  var script: ScriptCstNode? = null

  val classes = mutableListOf<ClassCstNode>()

}