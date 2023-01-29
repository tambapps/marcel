package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class IndexedReferenceExpression(override var scope: Scope, val name: String,
  val indexArguments: List<ExpressionNode>): ExpressionNode, ScopedNode<Scope> {

  override val type: JavaType
    get() = TODO("Get arrayType.elementsType, or the return type of the getAt method for non array objeccts")

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    TODO("Not yet implemented")
  }

}