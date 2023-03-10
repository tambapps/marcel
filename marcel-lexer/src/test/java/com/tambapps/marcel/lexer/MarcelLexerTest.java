package com.tambapps.marcel.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.tambapps.marcel.lexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            token(TYPE_INT, "int"),
            token(IDENTIFIER, "myInt"),
            token(ASSIGNMENT),
            token(INTEGER, "898"),
            token(END_OF_FILE)
        )
        , lexer.lex("int myInt = 898"));
  }

  @Test
  public void testBoolDeclaration() {
    assertEquals(Arrays.asList(
            token(TYPE_BOOL, "bool"),
            token(IDENTIFIER, "myBool"),
            token(ASSIGNMENT),
            token(VALUE_TRUE),
            token(END_OF_FILE)
        )
        , lexer.lex("bool myBool = true"));
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

  @Test
  public void testAssignment() {
    List<LexToken> tokens = lexer.lex("int a = foo(bar, 2)");
    assertEquals(
        Arrays.asList(
            token(TYPE_INT, "int"),
            token(IDENTIFIER, "a"),
            token(ASSIGNMENT),
            token(IDENTIFIER, "foo"),
            token(LPAR),
            token(IDENTIFIER, "bar"),
            token(COMMA),
            token(INTEGER, "2"),
            token(RPAR),
            token(END_OF_FILE)
        ), tokens
    );
  }

  @Test
  public void testFunctionDefinition() {
    List<LexToken> tokens = lexer.lex("fun sum(int a, int b) { return a + b }");
    assertEquals(
        Arrays.asList(
            token(FUN),
            token(IDENTIFIER, "sum"),
            token(LPAR),
            token(TYPE_INT, "int"),
            token(IDENTIFIER, "a"),
            token(COMMA),
            token(TYPE_INT, "int"),
            token(IDENTIFIER, "b"),
            token(RPAR),
            token(BRACKETS_OPEN),
            token(RETURN),
            token(IDENTIFIER, "a"),
            token(PLUS),
            token(IDENTIFIER, "b"),
            token(BRACKETS_CLOSE),
            token(END_OF_FILE)
        ), tokens
    );
  }

  @Test
  public void testRegexString() {
    assertEquals(Arrays.asList(
        token(OPEN_REGEX_QUOTE),
        token(REGULAR_STRING_PART, "some$'\""),
        token(REGULAR_STRING_PART, "\\w"),
        token(ESCAPE_SEQUENCE, "\\/"),
        token(CLOSING_REGEX_QUOTE),
        token(END_OF_FILE)
    ), lexer.lex("/some$'\"\\w\\//"));
  }


  @Test
  public void testDiv() {
    assertEquals(Arrays.asList(
        token(INTEGER, "1"),
        token(DIV),
        token(IDENTIFIER, "a"),
        token(DIV),
        token(INTEGER, "2"),
        token(END_OF_FILE)
    ), lexer.lex("1 / a / 2"));
  }

  @Test
  public void testSimpleString() {
    assertEquals(Arrays.asList(
        token(OPEN_SIMPLE_QUOTE),
        token(REGULAR_STRING_PART, "some$/\""),
        token(ESCAPE_SEQUENCE, "\\'"),
        token(CLOSING_SIMPLE_QUOTE),
        token(END_OF_FILE)
    ), lexer.lex("'some$/\"\\''"));
  }

  @Test
  public void testFail() {
    MarcelLexerException e = assertThrows(MarcelLexerException.class, () -> lexer.lex("'some$/\"\\"));
    assertEquals(0, e.getLine());
    assertEquals(8, e.getColumn());
    System.out.println(e.getMessage());
  }


  private LexToken token(TokenType type) {
    return token(type, null);
  }
  private LexToken token(TokenType type, String value) {
    return new LexToken(0, 0, 0, 0, type, value);
  }
}