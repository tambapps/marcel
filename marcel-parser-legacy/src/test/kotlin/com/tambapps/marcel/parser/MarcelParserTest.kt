package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.Visibility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

class MarcelParserTest {

    private val lexer = MarcelLexer()
    private val typeResolver = AstNodeTypeResolver()
    private val type = typeResolver.defineClass(null, Visibility.PUBLIC, "Test", JavaType.Object, false, emptyList())
    private val scope = MethodScope(typeResolver, mutableListOf(), type, "test", emptyList(), JavaType.void, staticContext = false)

    @Test
    fun testExpression() {
        val parser = MarcelParser(typeResolver, listOf(
            lexToken(TokenType.INTEGER, "2"),
            lexToken(TokenType.MUL, null),
            lexToken(TokenType.INTEGER, "3"),
            lexToken(TokenType.END_OF_FILE, null)
        ))
        val result = parser.expression(Scope(typeResolver, type, staticContext = false))
        assertEquals(MulOperator(LexToken.dummy(), IntConstantNode(LexToken.dummy(), 2), IntConstantNode(LexToken.dummy(), 3)), result)
    }

    @Test
    fun testIntDeclaration() {
        val parser = parser("int a = 22")
        assertEquals(
            VariableDeclarationNode(LexToken.dummy(), scope, JavaType.int, "a", false,IntConstantNode(LexToken.dummy(), 22)),
            parser.statement(Scope(typeResolver, type, staticContext = false)))
    }

    @Test
    fun testBoolDeclaration() {
        val parser = parser("bool b = false")
        assertEquals(
            VariableDeclarationNode(LexToken.dummy(), scope, JavaType.boolean, "b", false, BooleanConstantNode(LexToken.dummy(), false)),
            parser.statement(Scope(typeResolver, type, staticContext = false)))
    }

    @Test
    fun testFunction() {
        val parser = parser("fun int foo(int a, String b) { return 1 }")
        val imports = mutableListOf<ImportNode>(WildcardImportNode("java.lang"))
        val classScope = Scope(typeResolver, imports, type, staticContext = false)
        val expected = MethodNode(
            LexToken.dummy(), Opcodes.ACC_PUBLIC, JavaType.Object, "foo",
            FunctionBlockNode(LexToken.dummy(), scope, mutableListOf(
                ReturnNode(LexToken.dummy(), scope, IntConstantNode(LexToken.dummy(), 1))
            )), mutableListOf(
                MethodParameterNode(JavaType.int, "a"),
                MethodParameterNode(JavaType.String, "b")), JavaType.int, MethodScope(classScope, "foo",
                listOf(MethodParameter(JavaType.int, "a"), MethodParameter(JavaType.int, "b")), JavaType.int, staticContext = false)
        , false, emptyList()
        )

        val actual = parser.method(ClassNode(LexToken.dummy(), classScope, Opcodes.ACC_PUBLIC, type, JavaType.Object,
            true, mutableListOf(), mutableListOf(), mutableListOf(), emptyList()), emptyList())
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
            parser.statement(Scope(typeResolver, type, staticContext = false)))
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