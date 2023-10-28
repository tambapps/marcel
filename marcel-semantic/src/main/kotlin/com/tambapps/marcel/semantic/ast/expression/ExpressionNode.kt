package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.type.JavaTyped

interface ExpressionNode: Ast2Node, JavaTyped {

  fun <T> accept(visitor: ExpressionNodeVisitor<T>): T

}