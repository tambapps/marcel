package com.tambapps.marcel.parser.exception

import com.tambapps.marcel.lexer.LexToken

open class MarcelSemanticException constructor(token: LexToken?, message: String) : RuntimeException(if (token != null)
  "Semantic error at token ${token.type} (line ${token.line + 1}, column ${token.column}): $message" else "Semantic error: $message") {

  constructor(message: String): this(LexToken.dummy(), message)

}