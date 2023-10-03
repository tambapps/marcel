package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class MarcelParser2Test {

    @Test
    fun testStatement() {
        assertEquals(
            ExpressionStatementCstNode(null,
                fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),),
                token(), token()
                )
            , parser("a(1, 2f, b)").statement())
        assertNotEquals(
            ExpressionStatementCstNode(null,
                int(1),
                token(), token()
                )
            , parser("a(1, 2f, b)").statement())

    }
    @Test
    fun testFunctionCall() {
        assertEquals(
            fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),)
            , parser("a(1, 2f, b)").atom())
        assertEquals(
            fCall(value = "zoo", castType = type("float"),
                namedArgumentNodes = listOf(Pair("foo", int(123)), Pair("bar", double(23.0))),)
            , parser("zoo<float>(foo: 123, bar: 23d)").atom())

        assertNotEquals(fCall(value = "a"), parser("a(1, 2f, b)").atom())
    }

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
        assertEquals(type(value = "int"),
            parser("int").parseType())
        assertEquals(type(value = "DynamicObject"),
            parser("dynobj").parseType())
        assertEquals(type(value = "list", genericTypes = listOf("int")),
            parser("list<int>").parseType())
        assertEquals(type(value = "list", genericTypes = listOf("int"), arrayDimensions = 2),
            parser("list<int>[][]").parseType())
        assertEquals(type(value = "DynamicObject", arrayDimensions = 1),
            parser("dynobj[]").parseType())
        assertEquals(type(value = "Optional", arrayDimensions = 1),
            parser("Optional[]").parseType())

        assertNotEquals(type(value = "Optional", arrayDimensions = 2),
            parser("Optional[]").parseType())
    }

    @Test
    fun testLiteralNumbers() {
        assertEquals(int(value = 1234), parser("1234").atom())
        assertEquals(long(value = 1234L), parser("1234l").atom())
        assertEquals(float(value = 1234f), parser("1234f").atom())
        assertEquals(float(value = 1234.45f), parser("1234.45f").atom())
        assertEquals(double(value = 1234.0), parser("1234d").atom())
        assertEquals(double(value = 1234.45), parser("1234.45d").atom())

        assertEquals(int(value = 10 * 16 + 6), parser("0xA6").atom())
        assertEquals(long(value = 0b0101L), parser("0b0101L").atom())

        assertNotEquals(int(value = 123), parser("1234").atom())
        assertNotEquals(long(value = 123L), parser("1234l").atom())
        assertNotEquals(float(value = 134f), parser("1234f").atom())
        assertNotEquals(float(value = 134.45f), parser("1234.45f").atom())
        assertNotEquals(double(value = 234.0), parser("1234d").atom())
        assertNotEquals(double(value = 1234.4), parser("1234.45d").atom())
    }

    private fun fCall(value: String, castType: TypeCstNode? = null, positionalArgumentNodes: List<CstExpressionNode> = emptyList(),
                      namedArgumentNodes: List<Pair<String, CstExpressionNode>> = emptyList()
    ) = FunctionCallCstNode(parent = null, value = value, castType = castType,
        positionalArgumentNodes = positionalArgumentNodes, namedArgumentNodes = namedArgumentNodes,
        tokenStart = token(), tokenEnd = token()
    )
    private fun type(value: String, genericTypes: List<String> = emptyList(), arrayDimensions: Int = 0) = TypeCstNode(null, value, genericTypes, arrayDimensions, token(), token())
    private fun int(value: Int) = IntCstNode(value = value, token = token())
    private fun float(value: Float) = FloatCstNode(value = value, token = token())
    private fun long(value: Long) = LongCstNode(value = value, token = token())
    private fun double(value: Double) = DoubleCstNode(value = value, token = token())
    private fun ref(name: String) = ReferenceCstNode(value = name, token = token(), parent = null)
    private fun parser(text: String) = MarcelParser2(MarcelLexer().lex(text))
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
}