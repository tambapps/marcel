package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.TestUtils.assertIsEqual
import com.tambapps.marcel.parser.TestUtils.assertIsNotEqual
import com.tambapps.marcel.parser.TestUtils.parser
import com.tambapps.marcel.parser.compose.StatementScope
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelStatementParserTest: StatementScope() {

    private val defaultAccess = AccessCstNode(null, LexToken.DUMMY, LexToken.DUMMY, false, false, false, TokenType.VISIBILITY_PUBLIC, false)
    private val classNode = RegularClassCstNode(
        SourceFileCstNode(LexToken.DUMMY, LexToken.DUMMY, null, emptyList()),
        LexToken.DUMMY, LexToken.DUMMY, defaultAccess, "Test", null, emptyList(), false, null)

    @Test
    fun testVariableDeclaration() {
        assertIsEqual(varDecl(type("int"), "a", int(1)), parser("int a = 1;").statement())
        assertIsEqual(varDecl(type("int"), "a", int(1)), parser("int a = 1").statement())
        assertNotEquals(varDecl(type("float"), "a", int(1)), parser("int a = 1").statement())
    }


    @ParameterizedTest
    @ValueSource(strings = ["fun int foo() -> println(1)", "fun int foo() { println(1)  }"]) // six numbers
    fun testMethod(text: String) {
        val parser = parser(text)
        val method = parser.method(classNode, emptyList(), defaultAccess)
        assertTrue(method is MethodCstNode)
        method as MethodCstNode
        assertEquals("foo", method.name)
        assertIsEqual(type("int"), method.returnTypeNode)
        assertIsEqual(emptyList<AnnotationCstNode>(), method.annotations)
        assertIsEqual(emptyList<MethodParameterCstNode>(), method.parameters)

        assertIsEqual(
            listOf(
                stmt(fCall("println", args = listOf(int(1))))
            ),
            method.statements
        )
    }

    @Test
    fun testMethodWithParameter1() {
        testMethodWithParameter("fun int bar(int zoo) -> 25 + zoo",
            stmt(plus( int(25), ref("zoo"))))
    }

    @Test
    fun testStartingWithComment() {
        val result = parser("// HelloWorld.mcl\n" +
            "println(\"Hello World!\")\n").statement()
        assertIsEqual(result, stmt(fCall("println", args = listOf(templateSting("Hello World!")))))
    }

    @Test
    fun testMethodWithParameter2() {
        testMethodWithParameter("fun int bar(int zoo) { return 25 + zoo }",
            returnStmt(plus( int(25), ref("zoo"))))
    }

    private fun testMethodWithParameter(text: String, expectedBlock: StatementCstNode) {
        val parser = parser(text)
        val method = parser.method(classNode, emptyList(), defaultAccess)
        assertTrue(method is MethodCstNode)
        method as MethodCstNode
        assertEquals("bar", method.name)
        assertEquals(1, method.parameters.size)
        val parameter = method.parameters.first()
        assertIsEqual(type("int"), parameter.type)
        assertEquals("zoo", parameter.name)
        assertFalse(parameter.thisParameter)
        assertIsEqual(type("int"), method.returnTypeNode)
        assertIsEqual(emptyList<AnnotationCstNode>(), method.annotations)

        assertIsEqual(
            listOf(
                expectedBlock
            ),
            method.statements
        )
    }

    @Test
    fun testManyStatements() {
        val parser = parser("println(1); return null")
        assertIsEqual(stmt(fCall(value = "println", args = listOf(int(1)),)), parser.statement())
        assertIsEqual(returnStmt(nullValue()), parser.statement())
    }

    @Test
    fun testReturn() {
        val expect = ReturnCstNode(null,
            fCall(value = "a", args = listOf(int(1), float(2f), ref("b")),),
            LexToken.DUMMY, LexToken.DUMMY
        )

        assertIsEqual(expect, parser("return a(1, 2f, b);").statement())
        assertIsEqual(expect, parser("return a(1, 2f, b)").statement())
        assertIsNotEqual(expect, parser("return a(1, 2, b)").statement())
    }

    @Test
    fun testStatement() {
        assertIsEqual(
            ExpressionStatementCstNode(null,
                fCall(value = "a", args = listOf(int(1), float(2f), ref("b")),),
                LexToken.DUMMY, LexToken.DUMMY
                )
            , parser("a(1, 2f, b);").statement())
        assertIsNotEqual(
            ExpressionStatementCstNode(null,
                int(1),
                LexToken.DUMMY, LexToken.DUMMY
                )
            , parser("a(1, 2f, b)").statement())

    }
}