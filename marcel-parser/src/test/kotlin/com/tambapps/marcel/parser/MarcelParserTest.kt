package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AnnotationNode
import com.tambapps.marcel.parser.cst.AccessNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallNode
import com.tambapps.marcel.parser.cst.expression.LambdaNode
import com.tambapps.marcel.parser.cst.expression.NotNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusNode
import com.tambapps.marcel.parser.cst.expression.WrappedExpressionNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatNode
import com.tambapps.marcel.parser.cst.expression.literal.IntNode
import com.tambapps.marcel.parser.cst.expression.literal.LongNode
import com.tambapps.marcel.parser.cst.expression.literal.NullNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.cst.statement.ReturnNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelParserTest {

    private val defaultAccess = AccessNode(null, token(), token(), false, false, false, TokenType.VISIBILITY_PUBLIC, false)
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
            left = minus(binaryOperator(TokenType.MUL, left = int(3), right = int(5))),
            right = int(1))
            , parser("- 3 * 5 && 1").expression())
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
            left = not(WrappedExpressionNode(binaryOperator(TokenType.AND, left = int(1), right = int(2)))),
            right = int(1))
            , parser("!(1 && 2) && 1").expression())

        assertEquals(binaryOperator(TokenType.AND,
            left = binaryOperator(TokenType.AND, left = not(int(1)), right = int(2)),
            right = int(1))
            , parser("!1 && 2 && 1").expression())

        assertEquals(WrappedExpressionNode(
            not(
                binaryOperator(TokenType.DOT, left = ref("c"), right = fCall("isTruthy"))
            )
        ), parser("(!c.isTruthy())").expression())
    }

    @Test
    fun testLambdaExplicit0Args() {
        val lambda = parser("{ -> }").atom()
        assertTrue(lambda is LambdaNode)
        lambda as LambdaNode

        assertTrue(lambda.explicit0Parameters)
        assertTrue(lambda.parameters.isEmpty())
    }

    @Test
    fun testLambdaArgs() {
        val lambda = parser("{ arg1, Integer arg2 -> }").atom()
        assertTrue(lambda is LambdaNode)
        lambda as LambdaNode

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
        assertTrue(lambda is LambdaNode)
        lambda as LambdaNode

        assertFalse(lambda.explicit0Parameters)
        assertEquals(
            listOf(lambdaParam(type = type("int"), name = "arg")),
            lambda.parameters
        )
    }

    @Test
    fun testFunctionCallWithLambdaArg() {
        val parser = parser("assertThrows(ErrorResponseException.class) { ->\n}")
        val fCall = parser.expression(null)
        assertTrue(fCall is FunctionCallNode)
        fCall as FunctionCallNode
        assertEquals("assertThrows", fCall.value)
        assertNull(fCall.castType)
        assertEquals(2, fCall.positionalArgumentNodes.size)
        assertEquals(classReference(type("ErrorResponseException")), fCall.positionalArgumentNodes.first())
        val lambdaArg = fCall.positionalArgumentNodes[1]
        assertTrue(lambdaArg is LambdaNode)
        lambdaArg as LambdaNode
        assertTrue(lambdaArg.explicit0Parameters)
        assertTrue(lambdaArg.parameters.isEmpty())
        assertTrue(fCall.namedArgumentNodes.isEmpty())
    }

    @ParameterizedTest
    @ValueSource(strings = ["fun int foo() -> println(1)", "fun int foo() { println(1)  }"]) // six numbers
    fun testMethod(text: String) {
        val parser = parser(text)
        val method = parser.method(null, emptyList(), defaultAccess)
        assertTrue(method is MethodNode)
        method as MethodNode
        assertEquals("foo", method.name)
        assertEquals(type("int"), method.returnTypeNode)
        assertEquals(emptyList<AnnotationNode>(), method.annotations)
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
    fun testMethodWithParameter2() {
        testMethodWithParameter("fun int bar(int zoo) { return 25 + zoo }",
            returnNode(binaryOperator(TokenType.PLUS, int(25), ref("zoo"))))
    }

    private fun testMethodWithParameter(text: String, expectedBlock: CstNode) {
        val parser = parser(text)
        val method = parser.method(null, emptyList(), defaultAccess)
        assertTrue(method is MethodNode)
        method as MethodNode
        assertEquals("bar", method.name)
        assertEquals(1, method.parameters.size)
        val parameter = method.parameters.first()
        assertEquals(type("int"), parameter.type)
        assertEquals("zoo", parameter.name)
        assertFalse(parameter.thisParameter)
        assertEquals(type("int"), method.returnTypeNode)
        assertEquals(emptyList<AnnotationNode>(), method.annotations)

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
        val expect = ReturnNode(null,
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
            ExpressionStatementNode(null,
                fCall(value = "a", positionalArgumentNodes = listOf(int(1), float(2f), ref("b")),),
                token(), token()
                )
            , parser("a(1, 2f, b);").statement())
        assertNotEquals(
            ExpressionStatementNode(null,
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
        assertEquals(type(value = "list", genericTypes = listOf(type("int"))),
            parser("list<int>").parseType())
        assertEquals(type(value = "list", genericTypes = listOf(type("int")), arrayDimensions = 2),
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

    private fun fCall(value: String, castType: TypeNode? = null, positionalArgumentNodes: List<ExpressionNode> = emptyList(),
                      namedArgumentNodes: List<Pair<String, ExpressionNode>> = emptyList()
    ) = FunctionCallNode(parent = null, value = value, castType = castType,
        positionalArgumentNodes = positionalArgumentNodes, namedArgumentNodes = namedArgumentNodes,
        tokenStart = token(), tokenEnd = token()
    )


    private fun minus(expr: ExpressionNode) = UnaryMinusNode(expr, null, token(), token())
    private fun not(expr: ExpressionNode) = NotNode(expr, null, token(), token())
    private fun binaryOperator(type: TokenType, left: ExpressionNode, right: ExpressionNode) =
        BinaryOperatorNode(type, left, right, null, token(), token())
    private fun indexAccess(owner: ExpressionNode, indexes: List<ExpressionNode>, isSafeAccess: Boolean = false) =
        IndexAccessNode(null, owner, indexes, isSafeAccess, token(), token())
    private fun varDecl(typeNode: TypeNode, name: String, expr: ExpressionNode?) = VariableDeclarationNode(typeNode, name, expr, null, token(), token())
    private fun stmt(expr: ExpressionNode) = ExpressionStatementNode(expressionNode = expr, tokenStart = token(), tokenEnd = token())
    private fun returnNode(expr: ExpressionNode? = null) = ReturnNode(expressionNode = expr, tokenStart = token(), tokenEnd = token())
    private fun nullValue() = NullNode(token = token())
    private fun type(value: String, genericTypes: List<TypeNode> = emptyList(), arrayDimensions: Int = 0) = TypeNode(null, value, genericTypes, arrayDimensions, token(), token())
    private fun int(value: Int) = IntNode(value = value, token = token())
    private fun float(value: Float) = FloatNode(value = value, token = token())
    private fun long(value: Long) = LongNode(value = value, token = token())
    private fun double(value: Double) = DoubleNode(value = value, token = token())
    private fun ref(name: String) = ReferenceNode(value = name, token = token(), parent = null)
    private fun parser(text: String) = MarcelParser("Test", MarcelLexer().lex(text))
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
    private fun lambdaParam(type: TypeNode? = null, name: String) = LambdaNode.MethodParameterCstNode(null, token(), token(), type, name)
    private fun methodParam(type: TypeNode, name: String, defaultValue: ExpressionNode? = null,
                            annotations: List<AnnotationNode> = emptyList(), thisParameter: Boolean = false
    ) = MethodParameterCstNode(null, token(), token(), name, type, defaultValue, annotations, thisParameter)
    private fun classReference(type: TypeNode) = ClassReferenceNode(null, type, token(), token())
}