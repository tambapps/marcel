package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.MethodScope


open class BlockNode(override val scope: MethodScope, val statements: List<StatementNode>) : ExpressionNode, ScopedNode<MethodScope> {

  // it is important it is a getter, because statements could be modified after this object being constructed
  override val type
    get() = statements.lastOrNull()?.type ?: JavaType.void

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "{\n" + statements.joinToString(transform = { "\n  $it" }) + "\n}"
  }

}

// need to differentiate both because we don't always want to push on stack values for "normal" block nodes
class FunctionBlockNode constructor(scope: MethodScope, statements: List<StatementNode>) : BlockNode(scope, statements) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}