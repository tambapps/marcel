package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.method.JavaMethod

class GetAtFunctionCallNode(
  javaMethod: JavaMethod,
  // in order to get it non-null
  val ownerNode: ExpressionNode,
  arguments: List<ExpressionNode>,
  token: LexToken,
) : FunctionCallNode(javaMethod, ownerNode, arguments, token, LexToken.DUMMY) {

}