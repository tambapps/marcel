package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.owner.StaticOwner
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

class MarcelParserTest {

    private val lexer = MarcelLexer()
    private val scope = MethodScope(emptyList(), "Test", JavaType.OBJECT.internalName, emptyList(), "test", emptyList(), JavaType.void)

    @Test
    fun testExpression() {
        val parser = MarcelParser(listOf(
            LexToken(TokenType.INTEGER, "2"),
            LexToken(TokenType.MUL, null),
            LexToken(TokenType.INTEGER, "3"),
            LexToken(TokenType.END_OF_FILE, null)
        ))
        val result = parser.expression(Scope())
        assertEquals(MulOperator(IntConstantNode(2), IntConstantNode(3)), result)
    }

    @Test
    fun testIntDeclaration() {
        val parser = parser("int a = 22")
        assertEquals(
            VariableDeclarationNode(scope, JavaType.int, "a", IntConstantNode(22)),
            parser.statement(Scope()))
    }

    @Test
    fun testBoolDeclaration() {
        val parser = parser("bool b = false")
        assertEquals(
            VariableDeclarationNode(scope, JavaType.boolean, "b", BooleanConstantNode(false)),
            parser.statement(Scope()))
    }

    @Test
    fun testFunction() {
        val parser = parser("fun foo(int a, String b) int { return 1 }")
        val imports = listOf<ImportNode>(WildcardImportNode("java.lang"))
        val classScope = Scope(imports, "Test", JavaType.OBJECT.internalName, emptyList())
        val expected = MethodNode(Opcodes.ACC_PUBLIC, JavaType.OBJECT, "foo",
            FunctionBlockNode(scope, listOf(
                ReturnNode(scope, IntConstantNode(1))
            )), mutableListOf(MethodParameter(JavaType.int, "a"),
            MethodParameter(JavaType.STRING, "b")), JavaType.int, MethodScope(classScope, "foo",
                listOf(MethodParameter(JavaType.int, "a"), MethodParameter(JavaType.int, "b")), JavaType.int)
            )

        val actual = parser.method(classScope, ClassNode(Opcodes.ACC_PUBLIC, JavaType("Test"), JavaType.OBJECT, mutableListOf()))
        // verifying method signature
        assertEquals(expected.toString(), actual.toString())

        assertEquals(expected.block.statements, actual.block.statements)
    }


    /* too painful with lateinit properties

@Test
fun testMethodAccess() {
    val parser = parser("myVariable.myMethod().myField")
    val type = JavaType("Test")
    assertEquals(
        AccessOperator(
            AccessOperator(VariableReferenceExpression(type, "myVariable"), FunctionCallNode(type, "myMethod")).apply {
                                                                                                                      this.type = type
            },
            VariableReferenceExpression(type, "myField")).apply {
                                                                this.type = type
        },
        parser.expression(Scope()))

    }
     */

    private fun tokens(s: String): List<LexToken> {
        return lexer.lex(s)
    }

    private fun parser(s: String): MarcelParser {
        return MarcelParser(tokens(s))
    }
}