package com.tambapps.marcel.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.tambapps.marcel.lexer.TokenType.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class MarcelLexerTest {

  private MarcelLexer lexer;

  @BeforeEach
  public void init() {
    lexer = new MarcelLexer();
  }

  @Test
  public void testPrintln() {
    assertEquals(Arrays.asList(token(IDENTIFIER, "println"),
            token(LPAR),
            token(INTEGER, "8"),
            token(RPAR),
            token(SEMI_COLON),
            token(END_OF_FILE)
        )
        , lexer.lex("println(8);"));
  }

  @Test
  public void testVarDeclaration() {
    assertEquals(Arrays.asList(
            token(TYPE_INT),
            token(IDENTIFIER, "myInt"),
            token(ASSIGNMENT),
            token(INTEGER, "898"),
            token(END_OF_FILE)
        )
        , lexer.lex("int myInt = 898"));
  }

  @Test
  public void testString() {
    List<LexToken> tokens = lexer.lex("\"mystring \\\"$variable ${variable2}\"");
    assertEquals(
        Arrays.asList(
            token(OPEN_QUOTE),
            token(REGULAR_STRING_PART, "mystring "),
            token(ESCAPE_SEQUENCE, "\\\""),
            token(SHORT_TEMPLATE_ENTRY_START),
            token(IDENTIFIER, "variable"),
            token(REGULAR_STRING_PART, " "),
            token(LONG_TEMPLATE_ENTRY_START),
            token(IDENTIFIER, "variable2"),
            token(LONG_TEMPLATE_ENTRY_END),
            token(CLOSING_QUOTE),
            token(END_OF_FILE)
        ), tokens
    );
  }

  private LexToken token(TokenType type) {
    return token(type, null);
  }
  private LexToken token(TokenType type, String value) {
    return new LexToken(type, value);
  }
}