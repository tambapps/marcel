package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

abstract class AbstractExpressionCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), ExpressionCstNode {

  override var isStatement = false
  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

}