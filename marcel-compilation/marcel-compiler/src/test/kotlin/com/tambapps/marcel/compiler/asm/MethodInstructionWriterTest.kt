package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.processor.MarcelSemantic
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
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import marcel.lang.Script
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class MethodInstructionWriterTest : FunSpec({

  lateinit var writer: MethodInstructionWriter

  val mv = mockk<MethodVisitor>(relaxed = true)

  beforeSpec {
    writer = MethodInstructionWriter(mv, JavaType.Object)
    clearMocks(mv)
  }

  test("should not pop when expression is VoidExpressionNode") {
    writer.visit(exprStmt(VoidExpressionNode(token())))
    verify(exactly = 0) { mv.visitInsn(Opcodes.POP) }
  }

  test("should pop when expression is not VoidExpressionNode") {
    writer.visit(exprStmt(fCall(JavaType.Object, "hashCode")))
    verify { mv.visitInsn(Opcodes.POP) }
  }

}) {
  companion object {
    private fun token() = LexToken(0, 0, 0, 0, TokenType.END_OF_FILE, "")

    private fun int(value: Int) = IntConstantNode(value = value, token = token())
    private fun float(value: Float) = FloatConstantNode(value = value, token = token())
    private fun long(value: Long) = LongConstantNode(value = value, token = token())
    private fun double(value: Double) = DoubleConstantNode(value = value, token = token())

    private fun `return`(node: ExpressionNode) =
      ReturnStatementNode(node, token(), token())

    private fun exprStmt(node: ExpressionNode) =
      ExpressionStatementNode(node, token(), token())

    private fun fCall(owner: JavaType, name: String, arguments: List<JavaType> = emptyList()): ExpressionNode {
      val resolver = MarcelSymbolResolver()
      val method = resolver.findMethod(owner, name, arguments)
        ?: error("Method $name not found on $owner with $arguments")
      return fCall(method)
    }

    private fun fCall(method: MarcelMethod): ExpressionNode {
      return FunctionCallNode(
        javaMethod = method,
        arguments = emptyList(),
        tokenStart = token(),
        tokenEnd = token(),
        owner = null
      )
    }

    private fun expr(text: String): ExpressionNode {
      val cstExpression = MarcelParser("Test", MarcelLexer().lex(text)).expression()
      val sourceFile = mockk<SourceFileCstNode>(relaxed = true)
      return cstExpression.accept(MarcelSemantic(MarcelSymbolResolver(), Script::class.javaType, sourceFile, "Test.mcl"))
    }

    private fun stmt(text: String): StatementNode {
      val cstStatement = MarcelParser("Test", MarcelLexer().lex(text)).statement()
      val sourceFile = mockk<SourceFileCstNode>(relaxed = true)
      return cstStatement.accept(MarcelSemantic(MarcelSymbolResolver(), Script::class.javaType, sourceFile, "Test.mcl"))
    }
  }
}