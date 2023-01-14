package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.compiler.scope.Scope
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableDeclarationNode
import com.tambapps.marcel.parser.visitor.StatementVisitor
import org.objectweb.asm.MethodVisitor

class StatementGenerator(private val mv: MethodVisitor, val scope: Scope): StatementVisitor {

  private val expressionGenerator = UnpushedExpressionGenerator(mv, scope)

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(expressionGenerator)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name)
    expressionGenerator.visit(variableDeclarationNode as VariableAssignmentNode)
  }

}