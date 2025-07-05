package com.tambapps.marcel.semantic.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AbstractAstNode
import com.tambapps.marcel.semantic.symbol.type.JavaType

abstract class AbstractExpressionNode constructor(tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractAstNode(tokenStart, tokenEnd),
  ExpressionNode {

  constructor(token: LexToken) : this(token, token)
  constructor(node: CstNode) : this(node.tokenStart, node.tokenEnd)
}