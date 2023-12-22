package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.cst.SourceFileNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.Script
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodInstructionWriterTest {

  private val mv = mock(MethodVisitor::class.java)
  private val typeResolver = JavaTypeResolver()
  private val writer = MethodInstructionWriter(mv, typeResolver, JavaType.Object)
  private val pushingWriter = PushingMethodExpressionWriter(mv, typeResolver, JavaType.Object)

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

  private fun fCall(owner: JavaType, name: String, arguments: List<JavaType> = emptyList()) = fCall(typeResolver.findMethod(owner, name, arguments)!!)
  private fun fCall(method: JavaMethod) = FunctionCallNode(javaMethod = method, arguments = emptyList(), tokenStart = token(), tokenEnd = token(), owner = null)
  private fun int(value: Int) = IntConstantNode(value = value, token = token())
  private fun float(value: Float) = FloatConstantNode(value = value, token = token())
  private fun long(value: Long) = LongConstantNode(value = value, token = token())
  private fun double(value: Double) = DoubleConstantNode(value = value, token = token())

  private fun `return`(node: ExpressionNode) = ReturnStatementNode(node, token(), token())
  private fun exprStmt(node: ExpressionNode) = ExpressionStatementNode(node, token(), token())
  private fun expr(text: String): ExpressionNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).expression()
    val sourceFile = mock(SourceFileNode::class.java)
    return cstExpression.accept(MarcelSemantic(JavaTypeResolver(), Script::class.javaType, sourceFile),)
  }
  private fun stmt(text: String): StatementNode {
    val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).statement()
    val sourceFile = mock(SourceFileNode::class.java)
    return cstExpression.accept(MarcelSemantic(JavaTypeResolver(), Script::class.javaType, sourceFile))
  }

  private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")
}