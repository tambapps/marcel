package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

open class EnumCstNode constructor(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  access: AccessCstNode,
  className: String, // full class name. also handles inner class names
  val names: List<String>
) : ClassCstNode(parentSourceFileNode, tokenStart, tokenEnd, access, className, null, emptyList(), null) {
  override val isEnum = true
}