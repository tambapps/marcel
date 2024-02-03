package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.type.JavaTyped

interface ExpressionNode: AstNode, JavaTyped {

  fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>): T

}