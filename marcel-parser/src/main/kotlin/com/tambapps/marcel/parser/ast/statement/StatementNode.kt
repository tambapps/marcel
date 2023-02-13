package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

interface StatementNode: AstNode {

  fun <T> accept(mv: AstNodeVisitor<T>): T

}