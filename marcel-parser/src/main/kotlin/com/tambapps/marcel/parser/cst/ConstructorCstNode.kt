package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class ConstructorCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: CstAccessNode,
) :
  AbstractMethodCstNode(parent, tokenStart, tokenEnd, accessNode) {


  override fun toString(): String {
    return "constructor" + parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")
  }
}