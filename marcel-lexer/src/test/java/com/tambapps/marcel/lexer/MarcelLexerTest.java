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
  public void testPrintln() {
    assertEquals(Arrays.asList(token(TokenType.IDENTIFIER, "println"),
            token(TokenType.LPAR),
            token(TokenType.INTEGER, "8"),
            token(TokenType.RPAR),
            token(TokenType.SEMI_COLON),
            token(TokenType.END_OF_FILE)
        )
        , lexer.lex("println(8);"));
  }

  @Test
  public void testVarDeclaration() {
    assertEquals(Arrays.asList(
            token(TokenType.TYPE_INT),
            token(TokenType.IDENTIFIER, "myInt"),
            token(TokenType.ASSIGNEMENT),
            token(TokenType.INTEGER, "898"),
            token(TokenType.END_OF_FILE)
        )
        , lexer.lex("int myInt = 898"));
  }

  private LexToken token(TokenType type) {
    return token(type, null);
  }
  private LexToken token(TokenType type, String value) {
    return new LexToken(type, value);
  }
}