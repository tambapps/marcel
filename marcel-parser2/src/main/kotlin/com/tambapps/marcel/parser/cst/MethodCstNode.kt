package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class MethodCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: CstAccessNode,
  val name: String,
  val returnTypeCstNode: TypeCstNode,
) :
  AbstractMethodCstNode(parent, tokenStart, tokenEnd, accessNode) {

}