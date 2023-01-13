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
    assertEquals(Arrays.asList(new LexToken(TokenType.IDENTIFIER, "println"),
            new LexToken(TokenType.LPAR),
            new LexToken(TokenType.INTEGER, "8"),
            new LexToken(TokenType.RPAR),
            new LexToken(TokenType.SEMI_COLON),
            new LexToken(TokenType.END_OF_FILE)
        )
        , lexer.lex("println(8);"));
  }
}