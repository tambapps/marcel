package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.variable.VariableDeclarationNode
import com.tambapps.marcel.parser.visitor.StatementVisitor
import org.objectweb.asm.MethodVisitor

class StatementGenerator(private val mv: MethodVisitor): StatementVisitor {

  private val expressionGenerator = UnpushedExpressionGenerator(mv)

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(expressionGenerator)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    TODO()
  }

}