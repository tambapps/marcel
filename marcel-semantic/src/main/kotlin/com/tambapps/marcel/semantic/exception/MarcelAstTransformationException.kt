package com.tambapps.marcel.semantic.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.transform.AstTransformation

class MarcelAstTransformationException(instance: AstTransformation, token: LexToken?, message: String?) :
  MarcelSemanticException(token, "Error while performing AST transformation ${instance.javaClass.simpleName}: $message")