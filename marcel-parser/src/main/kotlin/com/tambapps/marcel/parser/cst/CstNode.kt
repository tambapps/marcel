package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

/**
 * Concrete Syntax Tree node
 *
 * @property parent the parent of this node if any
 * @property tokenStart the starting [LexToken] of this node
 * @property tokenEnd the ending [LexToken] of this node
 * @property token the [LexToken] of this node
 *
 */
interface CstNode {

  val parent: CstNode?
  val tokenStart: LexToken
  val tokenEnd: LexToken

  val token get() = tokenStart

}