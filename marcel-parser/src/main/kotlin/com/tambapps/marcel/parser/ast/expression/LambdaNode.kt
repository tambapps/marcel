package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaType

// TODO type is not well chosen
class LambdaNode(val scope: MethodScope, val parameters: List<MethodParameter>, val blockNode: BlockNode): ExpressionNode {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    TODO("Not yet implemented")
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    blockNode.accept(visitor)
    // make sure the block has this lambda's body
    blockNode.accept(object :AstVisitor {
      override fun visit(node: AstNode) {
        if (node is ScopedNode<*>) node.trySetScope(scope)
      }
    })
  }
  override val type: JavaType
    get() = JavaType.Object
}