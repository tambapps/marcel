package com.tambapps.marcel.lexer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MarcelLexerTest {

    private lateinit var lexer: MarcelLexer

    @BeforeEach
    fun init() {
        lexer = MarcelLexer()
    }
    @Test
    fun test() {
        assertEquals(listOf(Token(TokenType.IDENTIFIER, "println"),
            Token(TokenType.QUOTE, null),
            Token(TokenType.TEXT, "Hello World"),
            Token(TokenType.QUOTE, null)
        )
            , lexer.lex("println(\"Hello World\")"))
    }
}