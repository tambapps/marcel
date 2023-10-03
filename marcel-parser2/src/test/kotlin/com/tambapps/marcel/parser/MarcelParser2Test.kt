package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class MarcelParser2Test {

    @Test
    fun testIndexAccess() {
        assertEquals(IndexAccessCstNode(null,
            ref("a"),
            listOf(ref("i")), false, token(), token()), parser("a[i]").atom())
        assertEquals(IndexAccessCstNode(null,
            ref("a"),
            listOf(ref("i"), ref("b"), ref("c")), false, token(), token()), parser("a[i, b, c]").atom())

        assertNotEquals(IndexAccessCstNode(null,
            ref("a"),
            listOf(ref("ii")), false, token(), token()), parser("a[i]").atom())

    }

    @Test
    fun testTypes() {
        assertEquals(TypeCstNode(null, "int", emptyList(), 0, token(), token()),
            parser("int").parseType())
        assertEquals(TypeCstNode(null, "DynamicObject", emptyList(), 0, token(), token()),
            parser("dynobj").parseType())
        assertEquals(TypeCstNode(null, "list", listOf("int"), 0, token(), token()),
            parser("list<int>").parseType())
        assertEquals(TypeCstNode(null, "list", listOf("int"), 2, token(), token()),
            parser("list<int>[][]").parseType())
        assertEquals(TypeCstNode(null, "DynamicObject", emptyList(), 1, token(), token()),
            parser("dynobj[]").parseType())
        assertEquals(TypeCstNode(null, "Optional", emptyList(), 1, token(), token()),
            parser("Optional[]").parseType())

        assertNotEquals(TypeCstNode(null, "Optional", emptyList(), 2, token(), token()),
            parser("Optional[]").parseType())
    }

    @Test
    fun testLiteralNumbers() {
        assertEquals(IntCstNode(value = 1234, token = token()), parser("1234").atom())
        assertEquals(LongCstNode(value = 1234L, token = token()), parser("1234l").atom())
        assertEquals(FloatCstNode(value = 1234f, token = token()), parser("1234f").atom())
        assertEquals(FloatCstNode(value = 1234.45f, token = token()), parser("1234.45f").atom())
        assertEquals(DoubleCstNode(value = 1234.0, token = token()), parser("1234d").atom())
        assertEquals(DoubleCstNode(value = 1234.45, token = token()), parser("1234.45d").atom())

        assertEquals(IntCstNode(value = 10 * 16 + 6, token = token()), parser("0xA6").atom())
        assertEquals(LongCstNode(value = 0b0101L, token = token()), parser("0b0101L").atom())

        assertNotEquals(IntCstNode(value = 123, token = token()), parser("1234").atom())
        assertNotEquals(LongCstNode(value = 123L, token = token()), parser("1234l").atom())
        assertNotEquals(FloatCstNode(value = 134f, token = token()), parser("1234f").atom())
        assertNotEquals(FloatCstNode(value = 134.45f, token = token()), parser("1234.45f").atom())
        assertNotEquals(DoubleCstNode(value = 234.0, token = token()), parser("1234d").atom())
        assertNotEquals(DoubleCstNode(value = 1234.4, token = token()), parser("1234.45d").atom())
    }

    private fun ref(name: String) = ReferenceCstNode(value = name, token = token(), parent = null)
    private fun parser(text: String) = MarcelParser2(MarcelLexer().lex(text))
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
}