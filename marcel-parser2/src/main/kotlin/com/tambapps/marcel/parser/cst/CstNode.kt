package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

/**
 * Concrete Syntax Tree (AKA Parser Tree) node
 */
abstract class CstNode(
  val parent: CstNode?,
  val tokenStart: LexToken,
  val tokenEnd: LexToken
) {

  open val value: Any? = null
  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

  val token get() = tokenStart
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CstNode

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
