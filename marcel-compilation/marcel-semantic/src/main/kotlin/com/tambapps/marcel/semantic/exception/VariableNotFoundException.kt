package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken

class VariableNotFoundException(token: LexToken, message: String) : MarcelSemanticException(token, message) {
}