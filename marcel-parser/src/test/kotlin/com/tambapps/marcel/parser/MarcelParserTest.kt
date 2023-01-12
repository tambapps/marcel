package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import org.junit.jupiter.api.Test

class MarcelParserTest {

    @Test
    fun test() {
        val parser = MarcelParser(listOf(
            LexToken(TokenType.IDENTIFIER, "println"),
            LexToken(TokenType.LPAR, null),
            LexToken(TokenType.INTEGER, "8"),
            LexToken(TokenType.RPAR, null)
        ))
        val result = parser.parse()
        println(result)
    }
}