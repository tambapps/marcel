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
    private val type = typeResolver.defineClass("Test", JavaType.Object, false, emptyList())
    private val scope = MethodScope(typeResolver, mutableListOf(), type, "test", emptyList(), JavaType.void)

    @Test
    fun testExpression() {
        val parser = MarcelParser(typeResolver, listOf(
            lexToken(TokenType.INTEGER, "2"),
            lexToken(TokenType.MUL, null),
            lexToken(TokenType.INTEGER, "3"),
            lexToken(TokenType.END_OF_FILE, null)
        ))
        val result = parser.expression(Scope(typeResolver, type))
        assertEquals(MulOperator(LexToken.dummy(), IntConstantNode(LexToken.dummy(), 2), IntConstantNode(LexToken.dummy(), 3)), result)
    }

    @Test
    fun testIntDeclaration() {
        val parser = parser("int a = 22")
        assertEquals(
            VariableDeclarationNode(LexToken.dummy(), scope, JavaType.int, "a", false,IntConstantNode(LexToken.dummy(), 22)),
            parser.statement(Scope(typeResolver, type)))
    }

    @Test
    fun testBoolDeclaration() {
        val parser = parser("bool b = false")
        assertEquals(
            VariableDeclarationNode(LexToken.dummy(), scope, JavaType.boolean, "b", false, BooleanConstantNode(LexToken.dummy(), false)),
            parser.statement(Scope(typeResolver, type)))
    }

    @Test
    fun testFunction() {
        val parser = parser("fun foo(int a, String b) int { return 1 }")
        val imports = mutableListOf<ImportNode>(WildcardImportNode("java.lang"))
        val classScope = Scope(typeResolver, imports, type)
        val expected = MethodNode(Opcodes.ACC_PUBLIC, JavaType.Object, "foo",
            FunctionBlockNode(LexToken.dummy(), scope, mutableListOf(
                ReturnNode(LexToken.dummy(), scope, IntConstantNode(LexToken.dummy(), 1))
            )), mutableListOf(MethodParameter(JavaType.int, "a"),
            MethodParameter(JavaType.String, "b")), JavaType.int, MethodScope(classScope, "foo",
                listOf(MethodParameter(JavaType.int, "a"), MethodParameter(JavaType.int, "b")), JavaType.int)
        , false)

        val actual = parser.method(ClassNode(LexToken.dummy(), classScope, Opcodes.ACC_PUBLIC, type, JavaType.Object, true, mutableListOf(), mutableListOf(), mutableListOf()))
        // verifying method signature
        assertEquals(expected.toString(), actual.toString())

        assertEquals(expected.block.statements, actual.block.statements)
    }

    @Test
    fun testGenricTypeVarDeclaration() {
        val parser = parser("Map<Integer, Object> b = null")

        assertEquals(
            VariableDeclarationNode(LexToken.dummy(), scope, JavaType.of(Map::class.java, listOf(
                JavaType.of(Class.forName("java.lang.Integer")),
                JavaType.of(Class.forName("java.lang.Object"))
            )), "b", false, NullValueNode()),
            parser.statement(Scope(typeResolver, type)))
    }

    @AfterEach
    fun dispose() {
        typeResolver.clear()
    }

    private fun tokens(s: String): List<LexToken> {
        return lexer.lex(s)
    }

    private fun parser(s: String): MarcelParser {
        return MarcelParser(typeResolver, tokens(s))
    }
    private fun lexToken(type: TokenType, value: String? = null): LexToken {
        return LexToken(0, 0, 0, 0, type, value)
    }
}