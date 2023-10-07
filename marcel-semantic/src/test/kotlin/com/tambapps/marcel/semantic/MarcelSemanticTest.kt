package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser2
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class MarcelSemanticTest {

  @Test
  fun testReturn() {
    assertEquals(`return`(int(value = 1234)), stmt("return 1234;"))
    assertEquals(`return`(int(value = 1234)), stmt("return 1234"))
    assertNotEquals(`return`(int(value = 1234)), stmt("return 1234d;"))
  }

  @Test
  fun testStatement() {
    assertEquals(exprStmt(int(value = 1234)), stmt("1234;"))
    assertEquals(exprStmt(int(value = 1234)), stmt("1234"))
    assertNotEquals(exprStmt(int(value = 1234)), stmt("1234d;"))
  }

  @Test
  fun testLiteral() {
    assertEquals(int(value = 1234), expr("1234"))
    assertEquals(long(value = 1234L), expr("1234l"))
    assertEquals(float(value = 1234f), expr("1234f"))
    assertEquals(float(value = 1234.45f), expr("1234.45f"))
    assertEquals(double(value = 1234.0), expr("1234d"))
    assertEquals(double(value = 1234.45), expr("1234.45d"))

    assertEquals(int(value = 10 * 16 + 6), expr("0xA6"))
    assertEquals(long(value = 0b0101L), expr("0b0101L"))

    assertNotEquals(int(value = 123), expr("1234"))
    assertNotEquals(long(value = 123L), expr("1234l"))
    assertNotEquals(float(value = 134f), expr("1234f"))
    assertNotEquals(float(value = 134.45f), expr("1234.45f"))
    assertNotEquals(double(value = 234.0), expr("1234d"))
    assertNotEquals(double(value = 1234.4), expr("1234.45d"))
  }


  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())

  private fun `return`(node: ExpressionNode) = ReturnStatementNode(node, token(), token())
  private fun exprStmt(node: ExpressionNode) = ExpressionStatementNode(node, token(), token())
  private fun expr(text: String): ExpressionNode {
    val cstExpression = MarcelParser2("Test", MarcelLexer().lex(text)).expression()
    val sourceFile = mock(SourceFileCstNode::class.java)
    return cstExpression.accept(MarcelSemantic(JavaTypeResolver(), sourceFile).exprVisitor)
  }
  private fun stmt(text: String): StatementNode {
    val cstExpression = MarcelParser2("Test", MarcelLexer().lex(text)).statement()
    val sourceFile = mock(SourceFileCstNode::class.java)
    return cstExpression.accept(MarcelSemantic(JavaTypeResolver(), sourceFile).stmtVisitor)
  }

  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

}