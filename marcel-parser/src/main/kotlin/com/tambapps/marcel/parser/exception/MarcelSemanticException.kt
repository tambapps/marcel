package com.tambapps.marcel.parser.exception

import com.tambapps.marcel.lexer.LexToken

class MarcelSemanticException constructor(message: String?) : RuntimeException(message) {

  constructor(token: LexToken, message: String): this("Semantic error at token ${token.type} (line ${token.line}, column ${token.column}): $message")

}