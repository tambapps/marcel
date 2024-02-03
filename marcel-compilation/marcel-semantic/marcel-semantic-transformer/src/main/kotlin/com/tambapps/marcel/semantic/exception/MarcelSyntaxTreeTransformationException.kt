package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.transform.SyntaxTreeTransformation

class MarcelSyntaxTreeTransformationException(instance: SyntaxTreeTransformation, token: LexToken, message: String) :
  MarcelSemanticException(
    token,
    "Error while performing syntax tree transformation ${instance.javaClass.simpleName}: $message"
  )