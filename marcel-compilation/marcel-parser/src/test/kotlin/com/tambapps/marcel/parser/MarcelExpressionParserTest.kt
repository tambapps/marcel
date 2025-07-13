package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.TestUtils.assertIsEqual
import com.tambapps.marcel.parser.TestUtils.assertIsNotEqual
import com.tambapps.marcel.parser.TestUtils.parser
import com.tambapps.marcel.parser.compose.ExpressionScope
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelExpressionParserTest: ExpressionScope() {

    @Test
    fun testAllIn() {
        assertIsEqual(
            allIn(
                varType = type("int"),
                varName = "a",
                inExpr = ref("list"),
                filterExpr = goe(ref("a"), int(3))
            ),
            parser("when int a in list &> a >= 3").expression())
    }

    @Test
    fun testAnyIn() {
        assertIsEqual(
            anyIn(
                varType = type("int"),
                varName = "a",
                inExpr = ref("list"),
                filterExpr = goe(ref("a"), string("goo")),
                negate = true
            ),
            parser("!when int a in list |> a >= 'goo'").expression())
    }

    @Test
    fun testFindIn() {
        assertIsEqual(
            findIn(
                varType = type("int"),
                varName = "a",
                inExpr = ref("list"),
                filterExpr = goe(ref("a"), int(8)),
            ),
            parser("when int a in list -> a >= 8").expression())
    }

    @Test
    fun testMapFilter() {
        assertIsEqual(
            mapFilter(
                varType = type("int"),
                varName = "i",
                inExpr = ref("list"),
                mapExpr = plus(ref("i"), float(0.1f)),
                filterExpr = loe(ref("i"), char('2')),
            ),
            parser("[for int i in list -> i + 0.1f if i <= `2`]").expression())

        assertIsEqual(
            mapFilter(
                varType = type("int"),
                varName = "i",
                inExpr = ref("list"),
                mapExpr = plus(ref("i"), float(0.1f)),
            ),
            parser("[for int i in list -> i + 0.1f]").expression())
    }

    @Test
    fun testAsyncBlock() {
        assertIsEqual(
            async {  },
            parser("async {}").expression())
    }

    @Test
    fun testInstanceOf() {
        assertIsEqual(
            instanceof(ref("a"), type("Foo")),
            parser("a instanceof Foo").expression())

        assertIsEqual(
            notInstanceof(ref("b"), type("Bar")),
            parser("b !instanceof Bar").expression())
    }

    @Test
    fun testAs() {
        assertIsEqual(
            asType(ref("a"), type("Foo")),
            parser("a as Foo").expression())
    }

    @Test
    fun testElvisThrow() {
        assertIsEqual(
            elvisThrow(ref("a"), new(type("RuntimeException"))),
            parser("a ?: throw new RuntimeException()").expression())
    }

    @Test
    fun testNewInstance() {
        assertIsEqual(
            new(type("Foo"), args = listOf(int(1)), namedArgs = listOf("bar" to string(""))),
            parser("new Foo(1, bar: '')").expression())
    }

    @Test
    fun testSuperRef() {
        assertIsEqual(
            superRef(),
            parser("super").expression())
    }

    @Test
    fun testThisRef() {
        assertIsEqual(
            thisRef(),
            parser("this").expression())
    }


    @Test
    fun testSuperConstructorCall() {
        assertIsEqual(
            superConstrCall(args = listOf(int(1))),
            parser("super(1)").expression())
    }

    @Test
    fun testThisConstructorCall() {
        assertIsEqual(
            thisConstrCall(args = listOf(int(1))),
            parser("this(1)").expression())
    }

    @Test
    fun testIncr() {
        assertIsEqual(
            incr("foo", returnValueBefore = true),
            parser("foo++").expression())

        assertIsNotEqual(
            incr("foo", returnValueBefore = true),
            parser("++foo").expression())

        assertIsEqual(
            incr("foo", returnValueBefore = false),
            parser("++foo").expression())
    }

    @Test
    fun testTernary() {
        assertIsEqual(
            ternary(
                test = goe(ref("a"), ref("b")),
                trueExpr = ref("a"),
                falseExpr = ref("b")
            ),
            parser("a >= b ? a : b").expression())
    }

    @Test
    fun testDirectFieldRef() {
        assertIsEqual(
            directFieldRef("bar"),
            parser("@bar").expression())
    }

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
            left = eq(left = int(0), right = minus(int(1))),
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
    fun testMapNode() {
        assertIsEqual(map(
            WrappedExpressionCstNode(int(1)) to bool(false),
            string("foo") to string("bar")
        ),
            parser("[(1): false, foo: 'bar']").expression())
    }

    @Test
    fun testRegexNode() {
        assertIsEqual(regex("foo"), parser("r/foo/").expression())
        assertIsEqual(regex("foo", "dix"), parser("r/foo/dix").expression())
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

    @Test
    fun testWhen() {
        val expected = whenExpr {
            branch(eq(ref("a"), int(2))) {
                stmt(string("yay"))
            }
            branch(eq(ref("a"), int(3))) {
                stmt(bool(false))
            }
            elseBranch {
                stmt(int(0))
            }
        }

        assertIsEqual(expected, parser(
            """
                when {
                  a == 2 -> 'yay'
                  a == 3 -> false
                  else -> 0
                }
            """.trimIndent()
        ).expression())
    }

    @Test
    fun testSwitch() {
        val expected = switchExpr(ref("a")) {
            branch(int(2)) {
                stmt(string("yay"))
            }
            branch(int(3)) {
                stmt(bool(false))
            }
        }

        assertIsEqual(expected, parser(
            """
                switch (a) {
                  2 -> 'yay'
                  3 -> false
                }
            """.trimIndent()
        ).expression())
    }

    @Test
    fun testSwitchWithVarDecl() {
        val expected = switchExpr(
            varType = type("Integer"),
            varName = "a",
            isVarNullable = true,
            switchExpr = fCall("foo")
        ) {
            branch(int(2)) {
                stmt(string("yay"))
            }
            branch(int(3)) {
                stmt(bool(false))
            }
            elseBranch {
                stmt(int(0))
            }
        }

        assertIsEqual(expected, parser(
            """
                switch (Integer? a = foo()) {
                  2 -> 'yay'
                  3 -> false
                  else -> 0
                }
            """.trimIndent()
        ).expression())
    }

    @Test
    fun testTruthyVarDecl() {
        assertIsEqual(
            truthyVarDecl(type = type("String"), name = "foo", expr = fCall("bar")),
            parser("String foo = bar()").ifConditionExpression(null)
        )
    }
}