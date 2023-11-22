package com.tambapps.marcel.parser.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNode

open class MarcelSemanticLegacyException constructor(token: LexToken?, message: String) : RuntimeException(if (token != null)
  "Semantic error at token ${token.type} (line ${token.line + 1}, column ${token.column}): $message" else "Semantic error: $message") {

  val line = token?.line
  val column = token?.column
  constructor(message: String): this(LexToken.dummy(), message)
  constructor(node: AstNode?, message: String): this(node?.token, message)

}