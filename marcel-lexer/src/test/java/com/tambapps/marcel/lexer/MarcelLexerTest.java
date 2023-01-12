package com.tambapps.marcel.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MarcelLexerTest {

  private MarcelLexer lexer;

  @BeforeEach
  public void init() {
    lexer = new MarcelLexer();
  }

  @Test
  public void test() {
    assertEquals(Arrays.asList(new LexToken(TokenTypes.IDENTIFIER, "println"),
            new LexToken(TokenTypes.LPAR, null),
            new LexToken(TokenTypes.INTEGER, "8"),
            new LexToken(TokenTypes.RPAR, null)
        )
        , lexer.lex("println(8)"));
  }
}