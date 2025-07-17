package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.IdentifiableAstNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.visitor.IsSemanticallyEqualVisitor
import com.tambapps.marcel.semantic.symbol.NullAware
import com.tambapps.marcel.semantic.symbol.type.JavaTyped

interface ExpressionNode : AstNode, IdentifiableAstNode, JavaTyped, NullAware {

  fun <T> accept(visitor: ExpressionNodeVisitor<T>): T

  override fun isSemanticEqualTo(other: AstNode) = accept(IsSemanticallyEqualVisitor(other))
}