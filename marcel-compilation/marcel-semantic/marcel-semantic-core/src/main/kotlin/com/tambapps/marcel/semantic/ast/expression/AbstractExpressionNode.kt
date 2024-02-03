package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AbstractAstNode
import com.tambapps.marcel.semantic.type.JavaType

abstract class AbstractExpressionNode(override val type: JavaType, tokenStart: LexToken, tokenEnd: LexToken) : AbstractAstNode(tokenStart, tokenEnd),
  com.tambapps.marcel.semantic.ast.expression.ExpressionNode {

  constructor(type: JavaType, token: LexToken): this(type, token, token)
  constructor(type: JavaType, node: CstNode): this(type, node.tokenStart, node.tokenEnd)
}