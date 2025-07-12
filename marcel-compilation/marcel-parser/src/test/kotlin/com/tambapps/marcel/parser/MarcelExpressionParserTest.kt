package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.TestUtils.assertIsEqual
import com.tambapps.marcel.parser.TestUtils.assertIsNotEqual
import com.tambapps.marcel.parser.TestUtils.parser
import com.tambapps.marcel.parser.compose.ExpressionComposer
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelExpressionParserTest: ExpressionComposer() {

    @Test
    fun testUnaryMinusNode() {
        assertIsEqual(minus(int(1)), parser("-1").expression())
        assertIsEqual(
            and(
                left = minus(dot(left = ref("a"), right = fCall("isTruthy"))),
                right = int(1)
            ),
            parser("-a.isTruthy() && 1").expression()
        )

        assertIsEqual(
            and(
                left = mul(left = minus(int(3)), right = int(5)),
                right = int(1)
            ),
            parser("- 3 * 5 && 1").expression())


        assertIsEqual(and(
            left = isEqual(left = int(0), right = minus(int(1))),
            right = gt(
                left = int(2),
                right = int(0)
            )
        )
            , parser("0 == -1 && 2 > 0").expression())
    }

    @Test
    fun testArrayNode() {
        assertIsEqual(array(int(1), bool(false), string("foo")),
            parser("[1, false 'foo']").expression())
    }

    @Test
    fun testNotNode() {
        assertIsEqual(not(int(1)), parser("!1").expression())

        assertIsEqual(and(
            left = not(dot( left = ref("a"), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.isTruthy() && 1").expression())
        assertIsEqual(and(
            left = not(int(45)),
            right = int(1))
            , parser("!45 && 1").expression())

        assertIsEqual(and(
            left = not(dot( left = dot( left = ref("a"), right = ref("b")), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.b.isTruthy() && 1").expression())


        assertIsEqual(and(
            left = not(WrappedExpressionCstNode(and( left = int(1), right = int(2)))),
            right = int(1))
            , parser("!(1 && 2) && 1").expression())

        assertIsEqual(and(
            left = and( left = not(int(1)), right = int(2)),
            right = int(1))
            , parser("!1 && 2 && 1").expression())

        assertIsEqual(WrappedExpressionCstNode(
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

    @ParameterizedTest
    @ValueSource(strings = ["a(1, 2f, b)", "a 1, 2f, b"])
    fun testFunctionCall1(source: String) {
        assertIsEqual(
            fCall(value = "a", args = listOf(int(1), float(2f), ref("b")),)
            , parser(source).expression())
    }

    @Test
    fun testFunctionCall2() {
        assertIsEqual(
            fCall(value = "zoo", castType = type("float"),
                namedArgs = listOf(Pair("foo", int(123)), Pair("bar", double(23.0))),)
            , parser("zoo<float>(foo: 123, bar: 23d)").expression())
    }

    @ParameterizedTest
    @ValueSource(strings = ["println(await(compute()))", "println(await compute())", "println await(compute())"])
    fun testNestedFunctionCall(source: String) {
        assertIsEqual(
            fCall(value = "println", args = listOf(
                fCall(value = "await", args = listOf(
                    fCall("compute")
                ),)
            ),)
            , parser(source).expression())
    }

    @Test
    fun testFunctionCall2WithoutParenthesis() { // no cast type here
        assertIsEqual(
            fCall(value = "zoo", namedArgs = listOf(Pair("foo", int(123)), Pair("bar", double(23.0))),)
            , parser("zoo foo: 123, bar: 23d").expression())
    }

    @ParameterizedTest
    @ValueSource(strings = ["a(1, 2f, b)", "a 1, 2f, b"])
    fun testFunctionCall3(source: String) { // testing the equals method
        assertIsNotEqual(fCall(value = "a"), parser(source).expression())
    }

    @Test
    fun testIndexAccess() {
        assertIsEqual(indexAccess(ref("a"), listOf(ref("i"))), parser("a[i]").atom())
        assertIsEqual(indexAccess(ref("a"), listOf(ref("i")), isSafeAccess = true), parser("a?[i]").atom())
        assertIsEqual(indexAccess(ref("a"), listOf(ref("i"), ref("b"), ref("c"))), parser("a[i, b, c]").atom())

        assertIsNotEqual(indexAccess(ref("a"), listOf(ref("i"))), parser("a?[i]").atom())
        assertIsNotEqual(indexAccess(ref("a"), listOf(ref("i")), isSafeAccess = true), parser("a[i]").atom())
        assertIsNotEqual(indexAccess(ref("a"), listOf(ref("ii"))), parser("a[i]").atom())

    }

    @Test
    fun testTypes() {
        assertIsEqual(type(value = "int"),
            parser("int").parseType())
        assertIsEqual(type(value = "DynamicObject"),
            parser("dynobj").parseType())
        assertIsEqual(type(value = "List", genericTypes = listOf(type("int"))),
            parser("List<int>").parseType())
        assertIsEqual(type(value = "List", genericTypes = listOf(type("int")), arrayDimensions = 2),
            parser("List<int>[][]").parseType())
        assertIsEqual(type(value = "DynamicObject", arrayDimensions = 1),
            parser("dynobj[]").parseType())
        assertIsEqual(type(value = "Optional", arrayDimensions = 1),
            parser("Optional[]").parseType())

        assertIsNotEqual(type(value = "Optional", arrayDimensions = 2),
            parser("Optional[]").parseType())
    }

    @Test
    fun testLiteralNumbers() {
        assertIsEqual(int(value = 1234), parser("1234").atom())
        assertIsEqual(long(value = 1234L), parser("1234l").atom())
        assertIsEqual(float(value = 1234f), parser("1234f").atom())
        assertIsEqual(float(value = 1234.45f), parser("1234.45f").atom())
        assertIsEqual(double(value = 1234.0), parser("1234d").atom())
        assertIsEqual(double(value = 1234.45), parser("1234.45d").atom())

        assertIsEqual(int(value = 10 * 16 + 6), parser("0xA6").atom())
        assertIsEqual(long(value = 0b0101L), parser("0b0101L").atom())

        assertIsNotEqual(int(value = 123), parser("1234").atom())
        assertIsNotEqual(long(value = 123L), parser("1234l").atom())
        assertIsNotEqual(float(value = 134f), parser("1234f").atom())
        assertIsNotEqual(float(value = 134.45f), parser("1234.45f").atom())
        assertIsNotEqual(double(value = 234.0), parser("1234d").atom())
        assertIsNotEqual(double(value = 1234.4), parser("1234.45d").atom())
    }
}