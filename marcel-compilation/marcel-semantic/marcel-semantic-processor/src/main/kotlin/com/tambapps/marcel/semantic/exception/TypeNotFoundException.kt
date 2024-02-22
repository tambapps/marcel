package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken

class TypeNotFoundException(token: LexToken, message: String, eof: Boolean = false) :
  MarcelSemanticException(token, message, eof) {
}