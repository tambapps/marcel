package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.WildcardImportNode
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

class MarcelParserTest {

    private val lexer = MarcelLexer()
    private val typeResolver = AstNodeTypeResolver()
    private val type = JavaType.of("Test")
    private val scope = MethodScope(typeResolver, mutableListOf(), type, JavaType.Object, "test", emptyList(), JavaType.void)

    @Test
    fun testExpression() {
        val parser = MarcelParser(typeResolver, listOf(
            LexToken(TokenType.INTEGER, "2"),
            LexToken(TokenType.MUL, null),
            LexToken(TokenType.INTEGER, "3"),
            LexToken(TokenType.END_OF_FILE, null)
        ))
        val result = parser.expression(Scope(typeResolver, type))
        assertEquals(MulOperator(IntConstantNode(2), IntConstantNode(3)), result)
    }

    @Test
    fun testIntDeclaration() {
        val parser = parser("int a = 22")
        assertEquals(
            VariableDeclarationNode(scope, JavaType.int, "a", IntConstantNode(22)),
            parser.statement(Scope(typeResolver, type)))
    }

    @Test
    fun testBoolDeclaration() {
        val parser = parser("bool b = false")
        assertEquals(
            VariableDeclarationNode(scope, JavaType.boolean, "b", BooleanConstantNode(false)),
            parser.statement(Scope(typeResolver, type)))
    }

    @Test
    fun testFunction() {
        val parser = parser("fun foo(int a, String b) int { return 1 }")
        val imports = mutableListOf<ImportNode>(WildcardImportNode("java.lang"))
        val classScope = Scope(typeResolver, imports, type, JavaType.Object)
        val expected = MethodNode(Opcodes.ACC_PUBLIC, JavaType.Object, "foo",
            FunctionBlockNode(scope, listOf(
                ReturnNode(scope, IntConstantNode(1))
            )), mutableListOf(MethodParameter(JavaType.int, "a"),
            MethodParameter(JavaType.String, "b")), JavaType.int, MethodScope(classScope, "foo",
                listOf(MethodParameter(JavaType.int, "a"), MethodParameter(JavaType.int, "b")), JavaType.int)
        , false)

        val actual = parser.method(ClassNode(classScope, Opcodes.ACC_PUBLIC, type, JavaType.Object, mutableListOf(), mutableListOf()))
        // verifying method signature
        assertEquals(expected.toString(), actual.toString())

        assertEquals(expected.block.statements, actual.block.statements)
    }

    @Test
    fun testGenricTypeVarDeclaration() {
        val parser = parser("Type<Integer, Object> b = null")

        assertEquals(
            VariableDeclarationNode(scope, JavaType.defineClass("Type", JavaType.of(Script::class.java), false).withGenericTypes(listOf(
                JavaType.of(Class.forName("java.lang.Integer")),
                JavaType.of(Class.forName("java.lang.Object"))
            )), "b", NullValueNode()),
            parser.statement(Scope(typeResolver, type)))
    }

    @AfterEach
    fun dispose() {
        JavaType.clear()
    }

    private fun tokens(s: String): List<LexToken> {
        return lexer.lex(s)
    }

    private fun parser(s: String): MarcelParser {
        return MarcelParser(typeResolver, tokens(s))
    }
}