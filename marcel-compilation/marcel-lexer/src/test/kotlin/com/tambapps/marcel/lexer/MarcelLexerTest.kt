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
      token(0, 0, TokenType.IDENTIFIER, "println"),
      token(0, 7, TokenType.LPAR),
      token(0, 8, TokenType.INTEGER, "8"),
      token(0, 9, TokenType.RPAR),
      token(0, 10, TokenType.SEMI_COLON),
      token(0, 11, TokenType.END_OF_FILE)
    )
  }

  test("lex variable declaration") {
    lexer.lex("int myInt = 898") shouldBe listOf(
      token(0, 0, TokenType.TYPE_INT, "int"),
      token(0, 3, TokenType.WHITESPACE),
      token(0, 4, TokenType.IDENTIFIER, "myInt"),
      token(0, 9, TokenType.WHITESPACE),
      token(0, 10, TokenType.ASSIGNMENT),
      token(0, 11, TokenType.WHITESPACE),
      token(0, 12, TokenType.INTEGER, "898"),
      token(0, 15, TokenType.END_OF_FILE)
    )
  }

  test("lex bool variable declaration") {
    lexer.lex("bool myBool = true") shouldBe listOf(
      token(0, 0, TokenType.TYPE_BOOL, "bool"),
      token(0, 4, TokenType.WHITESPACE),
      token(0, 5, TokenType.IDENTIFIER, "myBool"),
      token(0, 11, TokenType.WHITESPACE),
      token(0, 12, TokenType.ASSIGNMENT),
      token(0, 13, TokenType.WHITESPACE),
      token(0, 14, TokenType.VALUE_TRUE),
      token(0, 18, TokenType.END_OF_FILE)
    )
  }

    test("lex string") {
      lexer.lex("\"mystring \\\"\$variable \${variable2}\"") shouldBe listOf(
        token(0, 0, TokenType.OPEN_QUOTE),
        token(0, 1, TokenType.REGULAR_STRING_PART, "mystring "),
        token(0, 10, TokenType.ESCAPE_SEQUENCE, "\\\""),
        token(0, 12, TokenType.SHORT_TEMPLATE_ENTRY_START),
        token(0, 13, TokenType.IDENTIFIER, "variable"),
        token(0, 21, TokenType.REGULAR_STRING_PART, " "),
        token(0, 22, TokenType.LONG_TEMPLATE_ENTRY_START),
        token(0, 24, TokenType.IDENTIFIER, "variable2"),
        token(0, 33, TokenType.LONG_TEMPLATE_ENTRY_END),
        token(0, 34, TokenType.CLOSING_QUOTE),
        token(0, 35, TokenType.END_OF_FILE)
      )
    }

    test("lex variable assignment") {
      lexer.lex("int a = foo(bar, 2)") shouldBe listOf(
        token(0, 0, TokenType.TYPE_INT, "int"),
        token(0, 3, TokenType.WHITESPACE),
        token(0, 4, TokenType.IDENTIFIER, "a"),
        token(0, 5, TokenType.WHITESPACE),
        token(0, 6, TokenType.ASSIGNMENT),
        token(0, 7, TokenType.WHITESPACE),
        token(0, 8, TokenType.IDENTIFIER, "foo"),
        token(0, 11, TokenType.LPAR),
        token(0, 12, TokenType.IDENTIFIER, "bar"),
        token(0, 15, TokenType.COMMA),
        token(0, 16, TokenType.WHITESPACE),
        token(0, 17, TokenType.INTEGER, "2"),
        token(0, 18, TokenType.RPAR),
        token(0, 19, TokenType.END_OF_FILE)
      )
    }

    test("lex function") {
      lexer.lex("fun sum(int a, int b) {\nreturn a + b\n}") shouldBe listOf(
        token(0, 0, TokenType.FUN),
        token(0, 3, TokenType.WHITESPACE),
        token(0, 4, TokenType.IDENTIFIER, "sum"),
        token(0, 7, TokenType.LPAR),
        token(0, 8, TokenType.TYPE_INT, "int"),
        token(0, 11, TokenType.WHITESPACE),
        token(0, 12, TokenType.IDENTIFIER, "a"),
        token(0, 13, TokenType.COMMA),
        token(0, 14, TokenType.WHITESPACE),
        token(0, 15, TokenType.TYPE_INT, "int"),
        token(0, 18, TokenType.WHITESPACE),
        token(0, 19, TokenType.IDENTIFIER, "b"),
        token(0, 20, TokenType.RPAR),
        token(0, 21, TokenType.WHITESPACE),
        token(0, 22, TokenType.BRACKETS_OPEN),
        token(0, 23, TokenType.LINE_RETURN),
        token(1, 0, TokenType.RETURN),
        token(1, 6, TokenType.WHITESPACE),
        token(1, 7, TokenType.IDENTIFIER, "a"),
        token(1, 8, TokenType.WHITESPACE),
        token(1, 9, TokenType.PLUS),
        token(1, 10, TokenType.WHITESPACE),
        token(1, 11, TokenType.IDENTIFIER, "b"),
        token(1, 12, TokenType.LINE_RETURN),
        token(2, 0, TokenType.BRACKETS_CLOSE),
        token(2, 1, TokenType.END_OF_FILE)
      )
    }

    test("lex regex string") {
      lexer.lex("r/some$'\"\\w\\//") shouldBe listOf(
        token(0, 0, TokenType.OPEN_REGEX_QUOTE),
        token(0, 2, TokenType.REGULAR_STRING_PART, "some$'\""),
        token(0, 9, TokenType.REGULAR_STRING_PART, "\\w"),
        token(0, 11, TokenType.ESCAPE_SEQUENCE, "\\/"),
        token(0, 13, TokenType.CLOSING_REGEX_QUOTE),
        token(0, 14, TokenType.END_OF_FILE)
      )
    }

    test("lex div") {
      lexer.lex("1 / a / 2") shouldBe listOf(
        token(0, 0, TokenType.INTEGER, "1"),
        token(0, 1, TokenType.WHITESPACE),
        token(0, 2, TokenType.DIV),
        token(0, 3, TokenType.WHITESPACE),
        token(0, 4, TokenType.IDENTIFIER, "a"),
        token(0, 5, TokenType.WHITESPACE),
        token(0, 6, TokenType.DIV),
        token(0, 7, TokenType.WHITESPACE),
        token(0, 8, TokenType.INTEGER, "2"),
        token(0, 9, TokenType.END_OF_FILE)
      )
    }

    test("lex simple string") {
      lexer.lex("'some$/\"\\''") shouldBe listOf(
        token(0, 0, TokenType.OPEN_SIMPLE_QUOTE),
        token(0, 1, TokenType.REGULAR_STRING_PART, "some$/\""),
        token(0, 8, TokenType.ESCAPE_SEQUENCE, "\\'"),
        token(0, 10, TokenType.CLOSING_SIMPLE_QUOTE),
        token(0, 11, TokenType.END_OF_FILE)
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
    fun token(line: Int, column: Int, type: TokenType, value: String? = null): LexToken =
      LexToken(0, 0, line, column, type, value)
  }
}