package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelParserTest {

    private val defaultAccess = AccessCstNode(null, token(), token(), false, false, false, TokenType.VISIBILITY_PUBLIC, false)
    private val classNode = RegularClassCstNode(
        SourceFileCstNode(LexToken.DUMMY, LexToken.DUMMY, null, emptyList()),
        token(), token(), defaultAccess, "Test", null, emptyList(), false, null)

    @Test
    fun testVariableDeclaration() {
        assertEquals(varDecl(type("int"), "a", int(1)), parser("int a = 1;").statement())
        assertEquals(varDecl(type("int"), "a", int(1)), parser("int a = 1").statement())
        assertNotEquals(varDecl(type("float"), "a", int(1)), parser("int a = 1").statement())
    }

    @Test
    fun testUnaryMinusNode() {
        assertEquals(minus(int(1)), parser("-1").expression())
        assertEquals(binaryOperator(TokenType.AND,
            left = minus(binaryOperator(TokenType.DOT, left = ref("a"), right = fCall("isTruthy"))),
            right = int(1))
            , parser("-a.isTruthy() && 1").expression())

        assertEquals(binaryOperator(TokenType.AND,
            left = binaryOperator(TokenType.MUL, left = minus(int(3)), right = int(5)),
            right = int(1))
            , parser("- 3 * 5 && 1").expression())


        assertEquals(binaryOperator(TokenType.AND,
            left = binaryOperator(TokenType.EQUAL, left = int(0), right = minus(int(1))),
            right = binaryOperator(TokenType.GT,
                left = int(2),
                right = int(0))
        )
            , parser("0 == -1 && 2 > 0").expression())
    }

    @Test
    fun testNotNode() {
        assertEquals(not(int(1)), parser("!1").expression())

        assertEquals(binaryOperator(TokenType.AND,
            left = not(binaryOperator(TokenType.DOT, left = ref("a"), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.isTruthy() && 1").expression())
        assertEquals(binaryOperator(TokenType.AND,
            left = not(int(45)),
            right = int(1))
            , parser("!45 && 1").expression())

        assertEquals(binaryOperator(TokenType.AND,
            left = not(binaryOperator(TokenType.DOT, left = binaryOperator(TokenType.DOT, left = ref("a"), right = ref("b")), right = fCall("isTruthy"))),
            right = int(1))
            , parser("!a.b.isTruthy() && 1").expression())


        assertEquals(binaryOperator(TokenType.AND,
            left = not(WrappedExpressionCstNode(binaryOperator(TokenType.AND, left = int(1), right = int(2)))),
            right = int(1))
            , parser("!(1 && 2) && 1").expression())

        assertEquals(binaryOperator(TokenType.AND,
            left = binaryOperator(TokenType.AND, left = not(int(1)), right = int(2)),
            right = int(1))
            , parser("!1 && 2 && 1").expression())

        assertEquals(WrappedExpressionCstNode(
            not(
                binaryOperator(TokenType.DOT, left = ref("c"), right = fCall("isTruthy"))
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
    @ValueSource(strings = ["fun int foo() -> println(1)", "fun int foo() { println(1)  }"]) // six numbers
    fun testMethod(text: String) {
        val parser = parser(text)
        val method = parser.method(classNode, emptyList(), defaultAccess)
        assertTrue(method is MethodCstNode)
        method as MethodCstNode
        assertEquals("foo", method.name)
        assertEquals(type("int"), method.returnTypeNode)
        assertEquals(emptyList<AnnotationCstNode>(), method.annotations)
        assertEquals(emptyList<MethodParameterCstNode>(), method.parameters)

        assertEquals(
            listOf(
                stmt(fCall("println", positionalArgumentNodes = listOf(int(1))))
            ),
            method.statements
        )
    }

    @Test
    fun testMethodWithParameter1() {
        testMethodWithParameter("fun int bar(int zoo) -> 25 + zoo",
            stmt(binaryOperator(TokenType.PLUS, int(25), ref("zoo"))))
    }

    @Test
    fun testStartingWithComment() {
        val result = parser("// HelloWorld.mcl\n" +
            "println(\"Hello World!\")\n").statement()
        assertEquals(result, stmt(fCall("println", positionalArgumentNodes = listOf(templateSting("Hello World!")))))
    }

    @Test
    fun testMethodWithParameter2() {
        testMethodWithParameter("fun int bar(int zoo) { return 25 + zoo }",
            returnNode(binaryOperator(TokenType.PLUS, int(25), ref("zoo"))))
    }

    private fun testMethodWithParameter(text: String, expectedBlock: CstNode) {
        val parser = parser(text)
        val method = parser.method(classNode, emptyList(), defaultAccess)
        assertTrue(method is MethodCstNode)
        method as MethodCstNode
        assertEquals("bar", method.name)
        assertEquals(1, method.parameters.size)
        val parameter = method.parameters.first()
        assertEquals(type("int"), parameter.type)
        assertEquals("zoo", parameter.name)
        assertFalse(parameter.thisParameter)
        assertEquals(type("int"), method.returnTypeNode)
        assertEquals(emptyList<AnnotationCstNode>(), method.annotations)

        assertEquals(
            listOf(
                expectedBlock
            ),
            method.statements
        )
    }

    @Test
    fun testManyStatements() {
        val parser = parser("println(1); return null")
        assertEquals(stmt(fCall(value = "println", positionalArgumentNodes = listOf(int(1)),)), parser.statement())
        assertEquals(returnNode(nullValue()), parser.statement())
    }

    @Test
    fun testReturn() {
        val expect = ReturnCstNode(null,
            fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),),
            token(), token()
        )

        assertEquals(expect, parser("return a(1, 2f, b);").statement())
        assertEquals(expect, parser("return a(1, 2f, b)").statement())
        assertNotEquals(expect, parser("return a(1, 2, b)").statement())
    }

    @Test
    fun testStatement() {
        assertEquals(
            ExpressionStatementCstNode(null,
                fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),),
                token(), token()
                )
            , parser("a(1, 2f, b);").statement())
        assertNotEquals(
            ExpressionStatementCstNode(null,
                int(1),
                token(), token()
                )
            , parser("a(1, 2f, b)").statement())

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

    private fun fCall(value: String, castType: TypeCstNode? = null, positionalArgumentNodes: List<ExpressionCstNode> = emptyList(),
                      namedArgumentNodes: List<Pair<String, ExpressionCstNode>> = emptyList()
    ) = FunctionCallCstNode(parent = null, value = value, castType = castType,
        positionalArgumentNodes = positionalArgumentNodes, namedArgumentNodes = namedArgumentNodes,
        tokenStart = token(), tokenEnd = token()
    )


    private fun minus(expr: ExpressionCstNode) = UnaryMinusCstNode(expr, null, token(), token())
    private fun not(expr: ExpressionCstNode) = NotCstNode(expr, null, token(), token())
    private fun binaryOperator(type: TokenType, left: ExpressionCstNode, right: ExpressionCstNode) =
        BinaryOperatorCstNode(type, left, right, null, token(), token())
    private fun indexAccess(owner: ExpressionCstNode, indexes: List<ExpressionCstNode>, isSafeAccess: Boolean = false) =
        IndexAccessCstNode(null, owner, indexes, isSafeAccess, token(), token())
    private fun varDecl(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?) = VariableDeclarationCstNode(typeNode, name, expr, null, token(), token())
    private fun stmt(expr: ExpressionCstNode) = ExpressionStatementCstNode(expressionNode = expr, tokenStart = token(), tokenEnd = token())
    private fun returnNode(expr: ExpressionCstNode? = null) = ReturnCstNode(expressionNode = expr, tokenStart = token(), tokenEnd = token())
    private fun nullValue() = NullCstNode(token = token())
    private fun type(value: String, genericTypes: List<TypeCstNode> = emptyList(), arrayDimensions: Int = 0) = TypeCstNode(null, value, genericTypes, arrayDimensions, token(), token())
    private fun int(value: Int) = IntCstNode(value = value, token = token())
    private fun string(value: Any) = StringCstNode(value = value.toString(), token = token())
    private fun templateSting(value: Any) = TemplateStringCstNode(expressions = listOf(string(value)), tokenStart = token(), tokenEnd = token(), parent = null)
    private fun float(value: Float) = FloatCstNode(value = value, token = token())
    private fun long(value: Long) = LongCstNode(value = value, token = token())
    private fun double(value: Double) = DoubleCstNode(value = value, token = token())
    private fun ref(name: String) = ReferenceCstNode(value = name, token = token(), parent = null)
    private fun parser(text: String) = MarcelParser("Test", MarcelLexer().lex(text))
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
    private fun lambdaParam(type: TypeCstNode? = null, name: String) = LambdaCstNode.MethodParameterCstNode(null, token(), token(), type, name)
    private fun methodParam(type: TypeCstNode, name: String, defaultValue: ExpressionCstNode? = null,
                            annotations: List<AnnotationCstNode> = emptyList(), thisParameter: Boolean = false
    ) = MethodParameterCstNode(null, token(), token(), name, type, defaultValue, annotations, thisParameter)
    private fun classReference(type: TypeCstNode) = ClassReferenceCstNode(null, type, token(), token())
}