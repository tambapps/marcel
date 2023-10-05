package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AbstractAst2Node

abstract class AbstractExpressionNode(tokenStart: LexToken, tokenEnd: LexToken) : AbstractAst2Node(tokenStart, tokenEnd), ExpressionNode {

  constructor(token: LexToken): this(token, token)
}