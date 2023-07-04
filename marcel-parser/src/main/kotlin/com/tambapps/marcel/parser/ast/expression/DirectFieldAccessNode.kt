package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope

class DirectFieldAccessNode(override val token: LexToken, override var scope: Scope, val name: String) : ExpressionNode, ScopedNode<Scope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "@$name"
  }
}