package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.MarcelMethod

class GetAtFunctionCallNode(
  javaMethod: MarcelMethod,
  // in order to get it non-null
  val ownerNode: ExpressionNode,
  arguments: List<ExpressionNode>,
  token: LexToken,
) : FunctionCallNode(
  javaMethod,
  ownerNode,
  arguments,
  token,
  LexToken.DUMMY
) {

  override fun withOwner(owner: ExpressionNode) =
    GetAtFunctionCallNode(javaMethod, owner, arguments, token)
}