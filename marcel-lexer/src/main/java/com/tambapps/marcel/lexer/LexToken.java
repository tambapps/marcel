package com.tambapps.marcel.lexer;


import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class LexToken {
  TokenType type;
  String value;
  public LexToken(TokenType type) {
    this(type, null);
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
    return builder.append("}").toString();
  }
}