package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

/**
 * Concrete Syntax Tree (AKA Parser Tree) node
 */
interface CstNode {
  val token: LexToken

}