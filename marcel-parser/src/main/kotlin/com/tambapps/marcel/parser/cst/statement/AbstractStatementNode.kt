package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

abstract class AbstractStatementNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), StatementNode {

  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

}