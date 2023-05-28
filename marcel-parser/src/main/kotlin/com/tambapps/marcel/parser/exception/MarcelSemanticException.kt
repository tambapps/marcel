package com.tambapps.marcel.parser.exception

import com.tambapps.marcel.lexer.LexToken

open class MarcelSemanticException constructor(token: LexToken, message: String) : RuntimeException("Semantic error at token ${token.type} (line ${token.line}, column ${token.column}): $message") {

  val line = token.line
  val column = token.column

  constructor(message: String): this(LexToken.dummy(), message)

}