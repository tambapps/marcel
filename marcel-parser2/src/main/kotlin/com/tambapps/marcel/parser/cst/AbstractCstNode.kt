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

  open val value: Any? = null
  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

  val token get() = tokenStart
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AbstractCstNode

    if (parent != other.parent) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = parent?.hashCode() ?: 0
    result = 31 * result + (value?.hashCode() ?: 0)
    return result
  }


}
