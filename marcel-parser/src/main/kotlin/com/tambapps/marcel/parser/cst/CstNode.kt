package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

interface CstNode {
  val parent: CstNode?
  val tokenStart: LexToken
  val tokenEnd: LexToken

  val token get() = tokenStart

}