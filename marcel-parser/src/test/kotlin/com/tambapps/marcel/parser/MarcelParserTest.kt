package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.scope.Scope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarcelParserTest {

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
}