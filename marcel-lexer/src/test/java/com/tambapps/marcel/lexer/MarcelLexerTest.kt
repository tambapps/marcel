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
        assertEquals(listOf(LexToken(TokenTypes.IDENTIFIER, "println"),
            LexToken(TokenTypes.QUOTE, null),
            LexToken(TokenTypes.INTEGER, "8"),
            LexToken(TokenTypes.QUOTE, null)
        )
            , lexer.lex("println(8)"))
    }
}