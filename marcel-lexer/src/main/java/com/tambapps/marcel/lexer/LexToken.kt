package com.tambapps.marcel.lexer

data class LexToken(val type: Int, val value: String?) {
  constructor(type: Int) : this(type, null)
}