package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken


abstract class AbstractAstNode(override val tokenStart: LexToken, override val tokenEnd: LexToken) : AstNode {

  constructor(token: LexToken) : this(token, token)
}