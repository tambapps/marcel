package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.imprt.CstImportNode

// only handle scripts for now
class SourceFileCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val packageName: String?, // without extension
): AbstractCstNode(null, tokenStart, tokenEnd) {

  var script: ScriptCstNode? = null

  val classes = mutableListOf<ClassCstNode>()
  val imports = mutableListOf<CstImportNode>()
  val extensionTypes = mutableListOf<TypeCstNode>()

}