package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.TestUtils.assertIsEqual
import com.tambapps.marcel.parser.TestUtils.assertIsNotEqual
import com.tambapps.marcel.parser.TestUtils.parser
import com.tambapps.marcel.parser.compose.CstStatementScope
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MarcelStatementParserTest: CstStatementScope() {

    private val classNode = RegularClassCstNode(
        SourceFileCstNode(LexToken.DUMMY, LexToken.DUMMY, null, emptyList()),
        LexToken.DUMMY, LexToken.DUMMY, access(), "Test", null, emptyList(), false, null)

    @Test
    fun testVariableDeclaration() {
        assertIsEqual(varDeclStmt(type("int"), "a", int(1)), parser("int a = 1;").statement())
        assertIsEqual(varDeclStmt(type("int"), "a", int(1)), parser("int a = 1").statement())
        assertNotEquals(varDeclStmt(type("float"), "a", int(1)), parser("int a = 1").statement())
    }


    @ParameterizedTest
    @ValueSource(strings = ["fun int foo() -> println(1)", "fun int foo() { println(1)  }"]) // six numbers
    fun testMethod(text: String) {
        val parser = parser(text)
        val method = parser.method(classNode, emptyList(), access())
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
        val method = parser.method(classNode, emptyList(), access())
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
    fun testFunctionCall() {
        assertIsEqual(
            stmt(
                fCall(value = "a", args = listOf(int(1), float(2f), ref("b")),),
            )
            , parser("a(1, 2f, b);").statement())
        assertIsNotEqual(
            stmt(int(1))
            , parser("a(1, 2f, b)").statement())
    }

    @Test
    fun testContinue() {
        assertIsEqual(
            continueStmt()
            , parser("continue;").statement())

        assertIsNotEqual(
            breakStmt()
            , parser("continue;").statement())
    }

    @Test
    fun testBreak() {
        assertIsEqual(
            breakStmt()
            , parser("break").statement())

        assertIsNotEqual(
            continueStmt()
            , parser("break").statement())
    }

    @Test
    fun testThrow() {
        assertIsEqual(
            throwStmt(new(type("RuntimeException")))
            , parser("throw new RuntimeException()").statement())
    }

    @Test
    fun testMultiVarDecl() {
        assertIsEqual(
            multiVarDeclStmt(
                listOf(
                    Triple(type("int"), "foo", false),
                    Triple(type("String"), "bar", true),
                    Triple(type("DynamicObject"), "zoo", false),
                ),
                ref("array")
            )
            , parser("def (int foo, String? bar, dynobj zoo) = array").statement())
    }

    @Test
    fun testWhile() {
        assertIsEqual(
            whileStmt(eq(ref("foo"), ref("bar"))) {
                stmt(fCall("println", args = listOf(int(1))))
            }
            , parser("while(foo == bar) { println(1) }").statement())
    }

    @Test
    fun testIf() {
        assertIsEqual(
            ifStmt(bool(true)) {
                trueStmt { stmt(fCall("bar")) }
            }
            , parser("if (true) bar()").statement())
    }

    @Test
    fun testForIn() {
        assertIsEqual(
            forInStmt(type("int"), "i", false, ref("array")) {
                stmt(fCall("println", args = listOf(ref("i"))))
            }
            , parser("for (int i in array) { println(i) }").statement())
    }

    @Test
    fun testForInMulti() {
        assertIsEqual(
            forMultiVarInStmt(listOf(
                Triple(type("int"), "i", false),
                Triple(type("Object"), "o", true),
            ), ref("array")) {
                stmt(fCall("println", args = listOf(ref("i"))))
            }
            , parser("for ((int i, Object? o) in array) { println(i) }").statement())
    }

    @Test
    fun testForVar() {
        assertIsEqual(
            forVarStmt(
                type("int"), "i", false, int(0), lt(ref("i"), int(10)), incr("i", returnValueBefore = true)) {
                stmt(fCall("println", args = listOf(ref("i"))))
            }
            , parser("for (int i = 0; i < 10; i++) { println(i) }").statement())
    }

    @Test
    fun testIfElse() {
        assertIsEqual(
            ifStmt(bool(true)) {
                trueStmt { stmt(fCall("bar")) }
                falseBlock {
                    stmt(fCall("println"))
                    stmt(ref("exit"))
                }
            }
            , parser("if (true) bar() else { println(); exit; }").statement())
    }

    @Test
    fun testTryFinally() {
        assertIsEqual(
            tryCatchStmt {
                tryBlock { stmt(fCall("println")) }
                finallyBlock { stmt(fCall("println", args = listOf(string("finally")))) }
            }
            , parser("try { println() } finally { println('finally') }").statement())
    }

    @Test
    fun testTryCatch() {
        assertIsEqual(
            tryCatchStmt {
                tryBlock { stmt(fCall("println")) }
                catchBlock(listOf(type("MyException"), type("MyException2")), "e") { stmt(fCall("println", args = listOf(string("finally")))) }
                catchBlock(listOf(type("Exception")), "e") {  }
            }
            , parser("try { println() } catch (MyException | MyException2 e) { println('finally') } catch (Exception e) {}").statement())
    }

    @Test
    fun testTryWithResources() {
        assertIsEqual(
            tryCatchStmt {
                resource(type("int"), false, "i", int(2))
                resource(type("Object"), true, "o", fCall("foo"))

                tryBlock { stmt(fCall("println")) }
                catchBlock(listOf(type("Exception")), "e") { stmt(fCall("println", args = listOf(string("noo")))) }
            }
            , parser("try (int i = 2; Object? o = foo()) { println() } catch (Exception e) { println('noo') }").statement())
    }

    @Test
    fun testDoWhile() {
        assertIsEqual(
            doWhileStmt(eq(ref("foo"), ref("bar"))) {
                stmt(fCall("println", args = listOf(int(1))))
            }
            , parser("do { println(1) } while (foo == bar)").statement())
    }

}