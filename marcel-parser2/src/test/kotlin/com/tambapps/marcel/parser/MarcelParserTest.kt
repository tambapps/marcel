package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarcelParserTest {

    @Test
    fun testLiteralNumbers() {
        assertEquals(IntCstNode(value = 1234, token = token()), parse("1234").atom())
        assertEquals(LongCstNode(value = 1234L, token = token()), parse("1234l").atom())
        assertEquals(FloatCstNode(value = 1234f, token = token()), parse("1234f").atom())
        assertEquals(FloatCstNode(value = 1234.45f, token = token()), parse("1234.45f").atom())
        assertEquals(DoubleCstNode(value = 1234.0, token = token()), parse("1234d").atom())
        assertEquals(DoubleCstNode(value = 1234.45, token = token()), parse("1234.45d").atom())

        assertEquals(IntCstNode(value = 10 * 16 + 6, token = token()), parse("0xA6").atom())
        assertEquals(LongCstNode(value = 0b0101L, token = token()), parse("0b0101L").atom())
    }

    private fun parse(text: String) = MarcelParser2(MarcelLexer().lex(text))
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
}