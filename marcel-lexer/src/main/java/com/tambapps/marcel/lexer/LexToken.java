package com.tambapps.marcel.lexer;


import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
public class LexToken {
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
    return builder.append("}").toString();
  }
}