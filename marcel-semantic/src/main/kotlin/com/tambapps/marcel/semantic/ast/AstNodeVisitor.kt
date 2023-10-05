package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor

interface AstNodeVisitor<T>: StatementNodeVisitor<T>, ExpressionNodeVisitor<T> {
}