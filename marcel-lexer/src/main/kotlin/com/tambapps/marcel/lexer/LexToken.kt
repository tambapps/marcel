package com.tambapps.marcel.lexer

class LexToken(
  val start: Int,
  val end: Int,
  val line: Int,
  val column: Int,
  val type: TokenType,
  val _value: String?
) {

  val value get() = _value ?: throw MarcelLexerException(line, column, "Expected non-null value in token")

  companion object {
    val DUMMY = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
  }

  override fun toString(): String {
    val builder = StringBuilder()
    builder.append("LexToken(type=")
      .append(type)
    if (_value != null) {
      builder.append(" ,value=")
        .append(_value)
    }
    return builder.append(")").toString()
  }

  fun infoString(): String {
    return if (_value != null) String.format("\"%s\" (%s)", _value, type) else type.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is LexToken) return false

    if (type != other.type) return false
    if (_value != other._value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + (_value?.hashCode() ?: 0)
    return result
  }
}