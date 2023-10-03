package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken

interface Ast2Node {
  val token: LexToken
}

abstract class AbstractAst2Node(override val token: LexToken): Ast2Node {

}