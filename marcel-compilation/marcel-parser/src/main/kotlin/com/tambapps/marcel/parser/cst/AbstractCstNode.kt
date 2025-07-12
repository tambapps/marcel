package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

/**
 * Concrete Syntax Tree (AKA Parser Tree) node
 */
abstract class AbstractCstNode(
  override val parent: CstNode?,
  override val tokenStart: LexToken,
  override val tokenEnd: LexToken
): CstNode {

  override val value: Any? = null
  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

}
