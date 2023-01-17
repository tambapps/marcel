package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

// TODO name should be an expression and called owner or something
class AccessMethodNode(val name: String,
                       val call: FunctionCallNode) : ExpressionNode {

  override lateinit var type: JavaType

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    TODO("Not yet implemented")
  }
}