package com.tambapps.marcel.semantic.processor.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.exception.MarcelSemanticException

class VariableAccessException(token: LexToken, message: String) : VariableRelatedException(token, message)