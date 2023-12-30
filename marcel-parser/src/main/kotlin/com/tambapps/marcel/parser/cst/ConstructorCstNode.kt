package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

class ConstructorCstNode(
  val parentClassNode: ClassCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  accessNode: AccessCstNode,
) :
  AbstractMethodCstNode(parentClassNode, tokenStart, tokenEnd, accessNode) {


  override fun toString(): String {
    return "constructor" + parameters.joinToString(separator = ", ", prefix = "(", postfix = ")")
  }
}