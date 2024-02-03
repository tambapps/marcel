package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken

interface AstNode {
  val tokenStart: LexToken
  val tokenEnd: LexToken

  val token get() = tokenStart

}