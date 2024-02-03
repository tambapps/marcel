package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod

class GetAtFunctionCallNode(
  javaMethod: JavaMethod,
  // in order to get it non-null
  val ownerNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
  token: LexToken,
) : com.tambapps.marcel.semantic.ast.expression.FunctionCallNode(javaMethod, ownerNode, arguments, token, LexToken.DUMMY) {

  override fun withOwner(owner: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) = GetAtFunctionCallNode(javaMethod, owner, arguments, token)
}