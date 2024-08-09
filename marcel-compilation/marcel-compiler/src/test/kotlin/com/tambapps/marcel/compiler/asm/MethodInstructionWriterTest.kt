package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import marcel.lang.Script
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodInstructionWriterTest {

  private val mv = mock(MethodVisitor::class.java)
  private val symbolResolver = MarcelSymbolResolver()
  private val writer = MethodInstructionWriter(mv, JavaType.Object)
  private val pushingWriter = PushingMethodExpressionWriter(mv, JavaType.Object)

  @Test
  fun testDontPopWhenExpressionStatementIsVoidExpression() {
    writer.visit(exprStmt(VoidExpressionNode(token())))
    verify(mv, never()).visitInsn(Opcodes.POP)
  }

  @Test
  fun testPopWhenExpressionStatementIsNotVoidExpression() {
    writer.visit(exprStmt(fCall(JavaType.Object, "hashCode")))
    verify(mv).visitInsn(Opcodes.POP)
  }

  private fun fCall(owner: JavaType, name: String, arguments: List<JavaType> = emptyList()) = fCall(symbolResolver.findMethod(owner, name, arguments)!!)
  private fun fCall(method: MarcelMethod) = com.tambapps.marcel.semantic.ast.expression.FunctionCallNode(
    javaMethod = method,
    arguments = emptyList(),
    tokenStart = token(),
    tokenEnd = token(),
    owner = null
  )
  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())

  private fun `return`(node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) = ReturnStatementNode(node, token(), token())
  private fun exprStmt(node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) = ExpressionStatementNode(node, token(), token())
  private fun expr(text: String): com.tambapps.marcel.semantic.ast.expression.ExpressionNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).expression()
    val sourceFile = mock(SourceFileCstNode::class.java)
    return cstExpression.accept(MarcelSemantic(MarcelSymbolResolver(), Script::class.javaType, sourceFile, "Test.mcl"),)
  }
  private fun stmt(text: String): StatementNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).statement()
    val sourceFile = mock(SourceFileCstNode::class.java)
    return cstExpression.accept(MarcelSemantic(MarcelSymbolResolver(), Script::class.javaType, sourceFile, "Test.mcl"))
  }

  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
}