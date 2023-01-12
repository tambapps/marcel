package com.tambapps.marcel.lexer;


import lombok.Value;

@Value
public class LexToken {
  int type;
  String value;
}