package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.scope.Variable

class IndexedReferenceExpression(token: LexToken, override var scope: Scope, val name: String,
  val indexArguments: List<ExpressionNode>, val isSafeIndex: Boolean): AbstractExpressionNode(token), ScopedNode<Scope> {

  val variable: Variable
    get() = scope.findVariableOrThrow(name, this)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    val safeString = if (isSafeIndex) "?" else ""
    return "$name$safeString[" + indexArguments.joinToString(separator = ", ") + "]"
  }
}