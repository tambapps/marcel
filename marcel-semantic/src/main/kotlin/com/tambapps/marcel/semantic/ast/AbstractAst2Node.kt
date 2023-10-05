package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken


abstract class AbstractAst2Node(override val tokenStart: LexToken, override val tokenEnd: LexToken): Ast2Node {

  constructor(token: LexToken): this(token, token)
}