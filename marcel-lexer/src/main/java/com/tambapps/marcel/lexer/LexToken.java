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
}