package com.tambapps.marcel.lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LexTokenTest: FunSpec({

  test("identical tokens should be equal") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "variable")
    val token2 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "variable")

    token1 shouldBe token2
    token1.hashCode() shouldBe token2.hashCode()
  }

  test("tokens with different values should not be equal") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "variable1")
    val token2 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "variable2")

    token1 shouldNotBe token2
    token1.hashCode() shouldNotBe token2.hashCode()
  }

  test("tokens with different types should not be equal") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "value")
    val token2 = LexToken(10, 15, 1, 5, TokenType.INTEGER, "value")

    token1 shouldNotBe token2
  }

  test("tokens with different line numbers should not be equal") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "value")
    val token2 = LexToken(10, 15, 2, 5, TokenType.IDENTIFIER, "value")

    token1 shouldNotBe token2
  }

  test("tokens with different column numbers should not be equal") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "value")
    val token2 = LexToken(10, 15, 1, 10, TokenType.IDENTIFIER, "value")

    token1 shouldNotBe token2
  }

  test("tokens with same line, column, type and value should be equal regardless of start/end position") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "value")
    val token2 = LexToken(20, 25, 1, 5, TokenType.IDENTIFIER, "value")

    token1 shouldBe token2
  }

  test("null values should be handled properly") {
    val token1 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, null)
    val token2 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, null)
    val token3 = LexToken(10, 15, 1, 5, TokenType.IDENTIFIER, "value")

    token1 shouldBe token2
    token1 shouldNotBe token3
  }

  test("DUMMY token should be consistent") {
    val dummy1 = LexToken.DUMMY
    val dummy2 = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

    dummy1 shouldBe dummy2
  }

  test("dummy factory method should create tokens with specified value") {
    val dummyToken = LexToken.dummy("test")

    dummyToken.getLine() shouldBe 0
    dummyToken.getColumn() shouldBe 0
    dummyToken.getType() shouldBe TokenType.END_OF_FILE
    dummyToken.getValue() shouldBe "test"
  }
})