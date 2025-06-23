package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.symbol.NullAware
import com.tambapps.marcel.semantic.symbol.type.JavaTyped
import com.tambapps.marcel.semantic.symbol.type.Nullness

interface ExpressionNode : AstNode, JavaTyped, NullAware {

  fun <T> accept(visitor: ExpressionNodeVisitor<T>): T

}