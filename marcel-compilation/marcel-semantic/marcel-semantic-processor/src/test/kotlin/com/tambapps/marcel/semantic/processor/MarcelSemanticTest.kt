package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.compose.AstExpressionScope
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.symbol.type.Nullness
import marcel.lang.Script
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

class MarcelSemanticTest: AstExpressionScope() {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
    private val CLASS_SCOPE =
      ClassScope(TYPE_RESOLVER, JavaType.Companion.Object, null, ImportResolver.Companion.DEFAULT_IMPORTS)
    private val METHOD = MethodNode(
      "foo",
      Nullness.UNKNOWN,
      mutableListOf(),
      Visibility.PUBLIC,
      JavaType.Companion.int,
      isStatic = false,
      LexToken.DUMMY,
      LexToken.DUMMY,
      JavaType.Companion.Object
    )
  }
  private val sourceFile = Mockito.mock(SourceFileCstNode::class.java)

  @Test
  fun testReturnInvalidType() {
    val node = Mockito.mock(ReturnCstNode::class.java)
    Mockito.`when`(node.token).thenReturn(token())
    Mockito.`when`(node.expressionNode).thenReturn(null)
    val semantic = semantic()
    semantic.scopeQueue.push(MethodScope(CLASS_SCOPE, METHOD))
    assertThrows<MarcelSemanticException> {
      semantic.visit(node)
      semantic.throwIfHasErrors()
    }
  }

  @Test
  fun testReturn() {
    val semantic = semantic()
    semantic.scopeQueue.push(MethodScope(CLASS_SCOPE, METHOD))

    Assertions.assertEquals(`return`(int(value = 1234)), stmt("return 1234;", semantic = semantic))
  }

  @Test
  fun testStatement() {
    Assertions.assertEquals(exprStmt(int(value = 1234)), stmt("1234;"))
    Assertions.assertEquals(exprStmt(int(value = 1234)), stmt("1234"))
    Assertions.assertNotEquals(exprStmt(int(value = 1234)), stmt("1234d;"))
  }

  @Test
  fun testLiteral() {
    Assertions.assertEquals(int(value = 1234), expr("1234"))
    Assertions.assertEquals(long(value = 1234L), expr("1234l"))
    Assertions.assertEquals(float(value = 1234f), expr("1234f"))
    Assertions.assertEquals(float(value = 1234.45f), expr("1234.45f"))
    Assertions.assertEquals(double(value = 1234.0), expr("1234d"))
    Assertions.assertEquals(double(value = 1234.45), expr("1234.45d"))

    Assertions.assertEquals(int(value = 10 * 16 + 6), expr("0xA6"))
    Assertions.assertEquals(long(value = 0b0101L), expr("0b0101L"))

    Assertions.assertNotEquals(int(value = 123), expr("1234"))
    Assertions.assertNotEquals(long(value = 123L), expr("1234l"))
    Assertions.assertNotEquals(float(value = 134f), expr("1234f"))
    Assertions.assertNotEquals(float(value = 134.45f), expr("1234.45f"))
    Assertions.assertNotEquals(double(value = 234.0), expr("1234d"))
    Assertions.assertNotEquals(double(value = 1234.4), expr("1234.45d"))
  }

  private fun semantic() = SourceFileSemanticProcessor(TYPE_RESOLVER, Script::class.javaType, sourceFile, "Test.mcl", NullSafetyMode.DISABLED)


  private fun `return`(node: ExpressionNode) = ReturnStatementNode(node, token(), token())
  private fun exprStmt(node: ExpressionNode) = ExpressionStatementNode(node, token(), token())

  private fun expr(text: String): ExpressionNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).expression()
    return cstExpression.accept(semantic())
  }
  private fun stmt(text: String, semantic: SourceFileSemanticProcessor = semantic()): StatementNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).statement()
    return cstExpression.accept(semantic)
  }

  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

}