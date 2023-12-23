package com.tambapps.marcel.lexer;


import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
@Value
public class LexToken {

  public static final LexToken DUMMY = new LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "");

  @EqualsAndHashCode.Exclude
  int start;
  @EqualsAndHashCode.Exclude
  int end;
  @EqualsAndHashCode.Exclude
  int line;
  @EqualsAndHashCode.Exclude
  int column;
  TokenType type;
  String value;

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
    return getValue() != null ? String.format("\"%s\" (%s)", value, type) : type.toString();
  }
}