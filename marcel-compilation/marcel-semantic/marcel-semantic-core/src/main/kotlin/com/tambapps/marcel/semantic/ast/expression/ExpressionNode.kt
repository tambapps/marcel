package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.symbol.type.JavaTyped

interface ExpressionNode : AstNode, JavaTyped {

  fun <T> accept(visitor: ExpressionNodeVisitor<T>): T

}