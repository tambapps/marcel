package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class AsNode constructor(token: LexToken,
                         override var scope: Scope,
                         val type: JavaType, val expressionNode: ExpressionNode): AbstractExpressionNode(token), ScopedNode<Scope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$expressionNode as ${type.simpleName}"
  }
}