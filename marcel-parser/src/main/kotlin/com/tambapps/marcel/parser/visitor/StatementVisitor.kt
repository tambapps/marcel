package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.ExpressionStatementNode

interface StatementVisitor {

  fun visit(expressionStatementNode: ExpressionStatementNode)

}