package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.compiler.exception.SemanticException
import com.tambapps.marcel.compiler.scope.Scope
import com.tambapps.marcel.parser.ast.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.variable.VariableDeclarationNode
import com.tambapps.marcel.parser.visitor.StatementVisitor
import org.objectweb.asm.MethodVisitor

class StatementGenerator(private val mv: MethodVisitor, private val scope: Scope): StatementVisitor {

  private val expressionGenerator = UnpushedExpressionGenerator(mv, scope)

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(expressionGenerator)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name)
    expressionGenerator.pushArgument(variableDeclarationNode.expressionNode)
    val (variable, index) = scope.getLocalVariableWithIndex(variableDeclarationNode.name)
    if (variable.type != variableDeclarationNode.expressionNode.type) {
      throw SemanticException("Incompatible types")
    }
    mv.visitVarInsn(variable.type.storeCode, index)

  }

}