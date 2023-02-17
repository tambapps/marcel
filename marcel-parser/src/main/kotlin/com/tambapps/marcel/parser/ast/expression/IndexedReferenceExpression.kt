package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaType

class IndexedReferenceExpression(override var scope: Scope, val name: String,
  val indexArguments: List<ExpressionNode>, val isSafeIndex: Boolean): ExpressionNode, ScopedNode<Scope> {

  val variable: Variable
    get() = scope.findVariable(name)

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    val safeString = if (isSafeIndex) "?" else ""
    return "$name$safeString[" + indexArguments.joinToString(separator = ", ") + "]"
  }
}