package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.MarcelSemantic
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import marcel.lang.Script
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class MarcelSemanticTest {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
    private val CLASS_SCOPE = ClassScope(TYPE_RESOLVER, JavaType.Object, null, ImportResolver.DEFAULT_IMPORTS)
    private val METHOD = MethodNode("foo", mutableListOf(),  Visibility.PUBLIC, JavaType.int, isStatic = false, LexToken.DUMMY, LexToken.DUMMY, JavaType.Object)
  }
  private val sourceFile = mock(SourceFileCstNode::class.java)

  @Test
  fun testReturnInvalidType() {
    val node = mock(ReturnCstNode::class.java)
    `when`(node.token).thenReturn(token())
    `when`(node.expressionNode).thenReturn(null)
    val semantic = semantic()
    semantic.scopeQueue.push(MethodScope(CLASS_SCOPE, METHOD))
    assertThrows<MarcelSemanticException> {
      semantic.visit(node)
    }
  }

  @Test
  fun testReturn() {
    val semantic = semantic()
    semantic.scopeQueue.push(MethodScope(CLASS_SCOPE, METHOD))

    assertEquals(`return`(int(value = 1234)), stmt("return 1234;", semantic = semantic))
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

  private fun semantic() = MarcelSemantic(TYPE_RESOLVER, Script::class.javaType, sourceFile, "Test.mcl")

  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())

  private fun `return`(node: ExpressionNode) = ReturnStatementNode(node, token(), token())
  private fun exprStmt(node: ExpressionNode) = ExpressionStatementNode(node, token(), token())

  private fun expr(text: String): ExpressionNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).expression()
    return cstExpression.accept(semantic())
  }
  private fun stmt(text: String, semantic: MarcelSemantic = semantic()): StatementNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).statement()
    return cstExpression.accept(semantic)
  }

  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

}