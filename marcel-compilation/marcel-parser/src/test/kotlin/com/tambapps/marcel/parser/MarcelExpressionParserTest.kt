package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.parser.compose.ExpressionComposer
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelExpressionParserTest: ExpressionComposer() {

    @Test
    fun testUnaryMinusNode() {
        assertEquals(minus(int(1)), parser("-1").expression())
        assertEquals(
            and(
                left = minus(dot(left = ref("a"), right = fCall("isTruthy"))),
                right = int(1)
            ),
            parser("-a.isTruthy() && 1").expression()
        )

        assertEquals(
            and(
                left = mul(left = minus(int(3)), right = int(5)),
                right = int(1)
            ),
            parser("- 3 * 5 && 1").expression())


        assertEquals(and(
            left = isEqual(left = int(0), right = minus(int(1))),
            right = gt(
                left = int(2),
                right = int(0)
            )
        )
            , parser("0 == -1 && 2 > 0").expression())
    }

    @Test
    fun testNotNode() {
        assertEquals(not(int(1)), parser("!1").expression())

        assertEquals(and(
            left = not(dot( left = ref("a"), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.isTruthy() && 1").expression())
        assertEquals(and(
            left = not(int(45)),
            right = int(1))
            , parser("!45 && 1").expression())

        assertEquals(and(
            left = not(dot( left = dot( left = ref("a"), right = ref("b")), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.b.isTruthy() && 1").expression())


        assertEquals(and(
            left = not(WrappedExpressionCstNode(and( left = int(1), right = int(2)))),
            right = int(1))
            , parser("!(1 && 2) && 1").expression())

        assertEquals(and(
            left = and( left = not(int(1)), right = int(2)),
            right = int(1))
            , parser("!1 && 2 && 1").expression())

        assertEquals(WrappedExpressionCstNode(
            not(
                dot( left = ref("c"), right = fCall("isTruthy"))
            )
        ), parser("(!c.isTruthy())").expression())
    }

    @Test
    fun testLambdaExplicit0Args() {
        val lambda = parser("{ -> }").atom()
        assertTrue(lambda is LambdaCstNode)
        lambda as LambdaCstNode

        assertTrue(lambda.explicit0Parameters)
        assertTrue(lambda.parameters.isEmpty())
    }

    @Test
    fun testLambdaArgs() {
        val lambda = parser("{ arg1, Integer arg2 -> }").atom()
        assertTrue(lambda is LambdaCstNode)
        lambda as LambdaCstNode

        assertFalse(lambda.explicit0Parameters)
        assertEquals(
            listOf(lambdaParam(name = "arg1"), lambdaParam(type = type("Integer"), name = "arg2")),
            lambda.parameters
        )
        // just to verify equals method
        assertNotEquals(
            listOf(lambdaParam(name = "not"), lambdaParam(type = type("Integer"), name = "arg2")),
            lambda.parameters
        )
    }

    @Test
    fun testLambdaPrimitiveArgs() {
        val lambda = parser("{ int arg -> }").atom()
        assertTrue(lambda is LambdaCstNode)
        lambda as LambdaCstNode

        assertFalse(lambda.explicit0Parameters)
        assertEquals(
            listOf(lambdaParam(type = type("int"), name = "arg")),
            lambda.parameters
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "assertThrows(ErrorResponseException.class) { ->\n}",
        "assertThrows ErrorResponseException.class \n{ ->\n}" // without parenthesis
    ]) // six numbers
    fun testFunctionCallWithLambdaArg(source: String) {
        val parser = parser(source)
        val fCall = parser.expression(null)
        assertTrue(fCall is FunctionCallCstNode)
        fCall as FunctionCallCstNode
        assertEquals("assertThrows", fCall.value)
        assertNull(fCall.castType)
        assertEquals(2, fCall.positionalArgumentNodes.size)
        assertEquals(classReference(type("ErrorResponseException")), fCall.positionalArgumentNodes.first())
        val lambdaArg = fCall.positionalArgumentNodes[1]
        assertTrue(lambdaArg is LambdaCstNode)
        lambdaArg as LambdaCstNode
        assertTrue(lambdaArg.explicit0Parameters)
        assertTrue(lambdaArg.parameters.isEmpty())
        assertTrue(fCall.namedArgumentNodes.isEmpty())
    }

    @ParameterizedTest
    @ValueSource(strings = ["a(1, 2f, b)", "a 1, 2f, b"])
    fun testFunctionCall1(source: String) {
        assertEquals(
            fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),)
            , parser(source).expression())
    }

    @Test
    fun testFunctionCall2() {
        assertEquals(
            fCall(value = "zoo", castType = type("float"),
                namedArgumentNodes = listOf(Pair("foo", int(123)), Pair("bar", double(23.0))),)
            , parser("zoo<float>(foo: 123, bar: 23d)").expression())
    }

    @ParameterizedTest
    @ValueSource(strings = ["println(await(compute()))", "println(await compute())", "println await(compute())"])
    fun testNestedFunctionCall(source: String) {
        assertEquals(
            fCall(value = "println", positionalArgumentNodes = listOf(
                fCall(value = "await", positionalArgumentNodes = listOf(
                    fCall("compute")
                ),)
            ),)
            , parser(source).expression())
    }

    @Test
    fun testFunctionCall2WithoutParenthesis() { // no cast type here
        assertEquals(
            fCall(value = "zoo", namedArgumentNodes = listOf(Pair("foo", int(123)), Pair("bar", double(23.0))),)
            , parser("zoo foo: 123, bar: 23d").expression())
    }

    @ParameterizedTest
    @ValueSource(strings = ["a(1, 2f, b)", "a 1, 2f, b"])
    fun testFunctionCall3(source: String) { // testing the equals method
        assertNotEquals(fCall(value = "a"), parser(source).expression())
    }

    @Test
    fun testIndexAccess() {
        assertEquals(indexAccess(ref("a"), listOf(ref("i"))), parser("a[i]").atom())
        assertEquals(indexAccess(ref("a"), listOf(ref("i")), isSafeAccess = true), parser("a?[i]").atom())
        assertEquals(indexAccess(ref("a"), listOf(ref("i"), ref("b"), ref("c"))), parser("a[i, b, c]").atom())

        assertNotEquals(indexAccess(ref("a"), listOf(ref("i"))), parser("a?[i]").atom())
        assertNotEquals(indexAccess(ref("a"), listOf(ref("i")), isSafeAccess = true), parser("a[i]").atom())
        assertNotEquals(indexAccess(ref("a"), listOf(ref("ii"))), parser("a[i]").atom())

    }

    @Test
    fun testTypes() {
        assertEquals(type(value = "int"),
            parser("int").parseType())
        assertEquals(type(value = "DynamicObject"),
            parser("dynobj").parseType())
        assertEquals(type(value = "List", genericTypes = listOf(type("int"))),
            parser("List<int>").parseType())
        assertEquals(type(value = "List", genericTypes = listOf(type("int")), arrayDimensions = 2),
            parser("List<int>[][]").parseType())
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

    private fun parser(text: String) = MarcelParser("Test", MarcelLexer().lex(text))
}