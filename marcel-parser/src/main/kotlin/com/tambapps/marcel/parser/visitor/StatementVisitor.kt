package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableDeclarationNode

interface StatementVisitor {

  fun visit(expressionStatementNode: ExpressionStatementNode)
  fun visit(variableDeclarationNode: VariableDeclarationNode)

}