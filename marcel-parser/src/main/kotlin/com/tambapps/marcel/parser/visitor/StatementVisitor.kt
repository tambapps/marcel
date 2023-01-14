package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.variable.VariableDeclarationNode

interface StatementVisitor {

  fun visit(expressionStatementNode: ExpressionStatementNode)
  fun visit(variableDeclarationNode: VariableDeclarationNode)

}