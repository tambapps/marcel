package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class ConstructorNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: AccessNode,
) :
  AbstractMethodNode(parent, tokenStart, tokenEnd, accessNode) {


  override fun toString(): String {
    return "constructor" + parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")
  }
}