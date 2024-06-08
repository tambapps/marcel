package com.tambapps.marcel.lexer

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class MarcelLexerTest {
  private var lexer: MarcelLexer? = null

  @BeforeEach
  fun init() {
    lexer = MarcelLexer()
  }

  @Test
  fun testPrintln() {
    Assertions.assertEquals(
      listOf(
        token(TokenType.IDENTIFIER, "println"),
        token(TokenType.LPAR),
        token(TokenType.INTEGER, "8"),
        token(TokenType.RPAR),
        token(TokenType.SEMI_COLON),
        token(TokenType.END_OF_FILE)
      ),
      lexer!!.lex("println(8);")
    )
  }

  @Test
  fun testVarDeclaration() {
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.TYPE_INT, "int"),
        whitespace(),
        token(TokenType.IDENTIFIER, "myInt"),
        whitespace(),
        token(TokenType.ASSIGNMENT),
        whitespace(),
        token(TokenType.INTEGER, "898"),
        token(TokenType.END_OF_FILE)
      ),
      lexer!!.lex("int myInt = 898")
    )
  }

  @Test
  fun testBoolDeclaration() {
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.TYPE_BOOL, "bool"),
        whitespace(),
        token(TokenType.IDENTIFIER, "myBool"),
        whitespace(),
        token(TokenType.ASSIGNMENT),
        whitespace(),
        token(TokenType.VALUE_TRUE),
        token(TokenType.END_OF_FILE)
      ),
      lexer!!.lex("bool myBool = true")
    )
  }

  @Test
  fun testString() {
    val tokens = lexer!!.lex("\"mystring \\\"\$variable \${variable2}\"")
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.OPEN_QUOTE),
        token(TokenType.REGULAR_STRING_PART, "mystring "),
        token(TokenType.ESCAPE_SEQUENCE, "\\\""),
        token(TokenType.SHORT_TEMPLATE_ENTRY_START),
        token(TokenType.IDENTIFIER, "variable"),
        token(TokenType.REGULAR_STRING_PART, " "),
        token(TokenType.LONG_TEMPLATE_ENTRY_START),
        token(TokenType.IDENTIFIER, "variable2"),
        token(TokenType.LONG_TEMPLATE_ENTRY_END),
        token(TokenType.CLOSING_QUOTE),
        token(TokenType.END_OF_FILE)
      ), tokens
    )
  }

  @Test
  fun testAssignment() {
    val tokens = lexer!!.lex("int a = foo(bar, 2)")
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.TYPE_INT, "int"),
        whitespace(),
        token(TokenType.IDENTIFIER, "a"),
        whitespace(),
        token(TokenType.ASSIGNMENT),
        whitespace(),
        token(TokenType.IDENTIFIER, "foo"),
        token(TokenType.LPAR),
        token(TokenType.IDENTIFIER, "bar"),
        token(TokenType.COMMA),
        whitespace(),
        token(TokenType.INTEGER, "2"),
        token(TokenType.RPAR),
        token(TokenType.END_OF_FILE)
      ), tokens
    )
  }

  @Test
  fun testFunctionDefinition() {
    val tokens = lexer!!.lex("fun sum(int a, int b) { return a + b }")
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.FUN),
        whitespace(),
        token(TokenType.IDENTIFIER, "sum"),
        token(TokenType.LPAR),
        token(TokenType.TYPE_INT, "int"),
        whitespace(),
        token(TokenType.IDENTIFIER, "a"),
        token(TokenType.COMMA),
        whitespace(),
        token(TokenType.TYPE_INT, "int"),
        whitespace(),
        token(TokenType.IDENTIFIER, "b"),
        token(TokenType.RPAR),
        whitespace(),
        token(TokenType.BRACKETS_OPEN),
        whitespace(),
        token(TokenType.RETURN),
        whitespace(),
        token(TokenType.IDENTIFIER, "a"),
        whitespace(),
        token(TokenType.PLUS),
        whitespace(),
        token(TokenType.IDENTIFIER, "b"),
        whitespace(),
        token(TokenType.BRACKETS_CLOSE),
        token(TokenType.END_OF_FILE)
      ), tokens
    )
  }

  @Test
  fun testRegexString() {
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.OPEN_REGEX_QUOTE),
        token(TokenType.REGULAR_STRING_PART, "some$'\""),
        token(TokenType.REGULAR_STRING_PART, "\\w"),
        token(TokenType.ESCAPE_SEQUENCE, "\\/"),
        token(TokenType.CLOSING_REGEX_QUOTE),
        token(TokenType.END_OF_FILE)
      ), lexer!!.lex("r/some$'\"\\w\\//")
    )
  }


  @Test
  fun testDiv() {
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.INTEGER, "1"),
        whitespace(),
        token(TokenType.DIV),
        whitespace(),
        token(TokenType.IDENTIFIER, "a"),
        whitespace(),
        token(TokenType.DIV),
        whitespace(),
        token(TokenType.INTEGER, "2"),
        token(TokenType.END_OF_FILE)
      ), lexer!!.lex("1 / a / 2")
    )
  }

  @Test
  fun testSimpleString() {
    Assertions.assertEquals(
      Arrays.asList(
        token(TokenType.OPEN_SIMPLE_QUOTE),
        token(TokenType.REGULAR_STRING_PART, "some$/\""),
        token(TokenType.ESCAPE_SEQUENCE, "\\'"),
        token(TokenType.CLOSING_SIMPLE_QUOTE),
        token(TokenType.END_OF_FILE)
      ), lexer!!.lex("'some$/\"\\''")
    )
  }

  @Test
  fun testFail() {
    val e = Assertions.assertThrows(MarcelLexerException::class.java) { lexer!!.lex("'some$/\"\\") }
    Assertions.assertEquals(0, e.line)
    Assertions.assertEquals(8, e.column)
    println(e.message)
  }


  private fun token(type: TokenType, value: String? = null): LexToken {
    return LexToken(0, 0, 0, 0, type, value)
  }
  private fun whitespace() = token(TokenType.WHITE_SPACE)
}