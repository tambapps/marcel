package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.MethodParameterNode
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class LambdaNode constructor(token: LexToken, override var scope: LambdaScope, val parameters: MutableList<MethodParameterNode>, val blockNode: BlockNode,
                             val explicit0Parameters: Boolean): AbstractExpressionNode(token), ScopedNode<LambdaScope> {

  override fun trySetScope(scope: Scope) {
    super.trySetScope(LambdaScope(scope))
  }

  var interfaceType: JavaType? = null
    set(value) {
      if (value != null && !value.isInterface) throw RuntimeException("Compiler error. This type should always be an interface")
      if (field != null && value != null && !field!!.isAssignableFrom(value)) {
        throw MarcelSemanticLegacyException(token, "Lambda is expected to bo of type ${field?.simpleName} and ${value.simpleName} which are not compatible types")
      }
      field = value
    }

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}