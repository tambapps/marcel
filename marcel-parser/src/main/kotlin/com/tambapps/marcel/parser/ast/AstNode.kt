package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType

interface AstNode {
  val token: LexToken
}

abstract class AbstractAstNode(override val token: LexToken): AstNode {

  // for testing
  constructor(): this(LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, ""))
}