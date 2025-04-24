package com.tambapps.marcel.lexer;

// need to keep it Java as it is used by MarcelJflexer

import java.util.Objects;

/**
 * Lexer Token
 */
public class LexToken {

  public static final LexToken DUMMY = new LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "");

  public static LexToken dummy(String value) {
    return new LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, value);
  }

  int start;
  int end;
  int line;
  int column;
  TokenType type;
  String value;

  public LexToken(int start, int end, int line, int column, TokenType type, String value) {
    this.start = start;
    this.end = end;
    this.line = line;
    this.column = column;
    this.type = type;
    this.value = value;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LexToken(type=")
        .append(type);
    if (value != null) {
      builder.append(" ,value=")
          .append(value);
    }
    return builder.append(")").toString();
  }
  
  public String infoString() {
    return getValue() != null ? String.format("\"%s\"", value) : type.toString();
  }

  public int getColumn() {
    return column;
  }

  public int getEnd() {
    return end;
  }

  public int getLine() {
    return line;
  }

  public int getStart() {
    return start;
  }

  public String getValue() {
    return value;
  }

  public TokenType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LexToken)) return false;
    LexToken lexToken = (LexToken) o;
    return type == lexToken.type && Objects.equals(value, lexToken.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, value);
  }
}