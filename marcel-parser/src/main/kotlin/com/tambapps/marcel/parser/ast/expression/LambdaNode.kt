package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.type.JavaType

class LambdaNode constructor(val scope: LambdaScope, val parameters: MutableList<MethodParameter>, val blockNode: BlockNode,
                             val explicit0Parameters: Boolean): ExpressionNode {

  var interfaceType: JavaType? = null
    set(value) {
      if (value != null && !value.isInterface) throw RuntimeException("Compiler error. This type should always be an interface")
      field = value
    }

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}