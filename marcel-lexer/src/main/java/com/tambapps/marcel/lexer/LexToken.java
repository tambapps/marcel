package com.tambapps.marcel.lexer;


import lombok.Value;

@Value
public class LexToken {
  TokenType type;
  String value;
}