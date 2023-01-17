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
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

class MarcelParserTest {

    private val lexer = MarcelLexer()

    @Test
    fun test() {
        val parser = MarcelParser(listOf(
            LexToken(TokenType.IDENTIFIER, "println"),
            LexToken(TokenType.LPAR, null),
            LexToken(TokenType.INTEGER, "8"),
            LexToken(TokenType.RPAR, null),
            LexToken(TokenType.END_OF_FILE, null)

        ))
        val result = parser.parse()
        println(result)
    }

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
    fun testAssignment() {
        val parser = parser("int a = 22")
        assertEquals(
            VariableDeclarationNode(JavaType.int, "a", IntConstantNode(22)),
            parser.statement(Scope()))
    }

    @Test
    fun testFunction() {
        val parser = parser("fun foo(int a, String b) int { return 1 }")
        val imports = listOf<ImportNode>(WildcardImportNode("java.lang"))
        val expected = MethodNode(Opcodes.ACC_PUBLIC, StaticOwner(JavaType.OBJECT), "foo",
            FunctionBlockNode(JavaType.int, listOf(
                ReturnNode(IntConstantNode(1))
            )), mutableListOf(MethodParameter(JavaType.int, "a"),
            MethodParameter(JavaType.STRING, "b")), JavaType.int, Scope())
        val actual = parser.method(imports, ClassNode(Opcodes.ACC_PUBLIC, "Test", JavaType.OBJECT, mutableListOf()))
        // verifying method signature
        assertEquals(expected.toString(), actual.toString())

        assertEquals(expected.block.statements, actual.block.statements)
    }

    private fun tokens(s: String): List<LexToken> {
        return lexer.lex(s)
    }

    private fun parser(s: String): MarcelParser {
        return MarcelParser(tokens(s))
    }
}