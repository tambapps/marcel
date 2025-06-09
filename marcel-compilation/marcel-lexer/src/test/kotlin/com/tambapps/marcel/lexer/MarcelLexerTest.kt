package com.tambapps.marcel.lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class MarcelLexerTest : FunSpec({

  lateinit var lexer: MarcelLexer

  beforeTest {
    lexer = MarcelLexer()
  }

  test("lex println") {
    lexer.lex("println(8);") shouldBe listOf(
      token(TokenType.IDENTIFIER, "println"),
      token(TokenType.LPAR),
      token(TokenType.INTEGER, "8"),
      token(TokenType.RPAR),
      token(TokenType.SEMI_COLON),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex variable declaration") {
    lexer.lex("int myInt = 898") shouldBe listOf(
      token(TokenType.TYPE_INT, "int"),
      whitespace(),
      token(TokenType.IDENTIFIER, "myInt"),
      whitespace(),
      token(TokenType.ASSIGNMENT),
      whitespace(),
      token(TokenType.INTEGER, "898"),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex bool variable declaration") {
    lexer.lex("bool myBool = true") shouldBe listOf(
      token(TokenType.TYPE_BOOL, "bool"),
      whitespace(),
      token(TokenType.IDENTIFIER, "myBool"),
      whitespace(),
      token(TokenType.ASSIGNMENT),
      whitespace(),
      token(TokenType.VALUE_TRUE),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex string") {
    lexer.lex("\"mystring \\\"\$variable \${variable2}\"") shouldBe listOf(
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
    )
  }

  test("lex variable assignment") {
    lexer.lex("int a = foo(bar, 2)") shouldBe listOf(
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
    )
  }

  test("lex function") {
    lexer.lex("fun sum(int a, int b) {\nreturn a + b\n}") shouldBe listOf(
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
      lineReturn(),
      token(TokenType.RETURN),
      whitespace(),
      token(TokenType.IDENTIFIER, "a"),
      whitespace(),
      token(TokenType.PLUS),
      whitespace(),
      token(TokenType.IDENTIFIER, "b"),
      lineReturn(),
      token(TokenType.BRACKETS_CLOSE),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex regex string") {
    lexer.lex("r/some$'\"\\w\\//") shouldBe listOf(
      token(TokenType.OPEN_REGEX_QUOTE),
      token(TokenType.REGULAR_STRING_PART, "some$'\""),
      token(TokenType.REGULAR_STRING_PART, "\\w"),
      token(TokenType.ESCAPE_SEQUENCE, "\\/"),
      token(TokenType.CLOSING_REGEX_QUOTE),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex div") {
    lexer.lex("1 / a / 2") shouldBe listOf(
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
    )
  }

  test("lex simple string") {
    lexer.lex("'some$/\"\\''") shouldBe listOf(
      token(TokenType.OPEN_SIMPLE_QUOTE),
      token(TokenType.REGULAR_STRING_PART, "some$/\""),
      token(TokenType.ESCAPE_SEQUENCE, "\\'"),
      token(TokenType.CLOSING_SIMPLE_QUOTE),
      token(TokenType.END_OF_FILE)
    )
  }

  test("lex fail") {
    val exception = shouldThrow<MarcelLexerException> {
      lexer.lex("'some$/\"\\")
    }
    exception.line shouldBe 0
    exception.column shouldBe 8
  }

}) {
  companion object {
    fun token(type: TokenType, value: String? = null): LexToken =
      LexToken(0, 0, 0, 0, type, value)

    fun whitespace() = token(TokenType.WHITESPACE)
    fun lineReturn() = token(TokenType.LINE_RETURN)
  }
}