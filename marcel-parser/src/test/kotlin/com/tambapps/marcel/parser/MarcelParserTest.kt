package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.expression.operator.binary.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
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
        val result = parser.expression()
        assertEquals(BinaryOperatorNode(TokenType.MUL).apply {
                                                             leftOperand = IntConstantNode(2)
                                                             rightOperand = IntConstantNode(3)
        }, result)
    }
}