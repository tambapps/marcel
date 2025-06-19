package com.tambapps.marcel.semantic.processor.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

class TypeCastException(token: LexToken, message: String) : MarcelSemanticException(token, message) {
  override val message: String
    get() = super.message!!
}