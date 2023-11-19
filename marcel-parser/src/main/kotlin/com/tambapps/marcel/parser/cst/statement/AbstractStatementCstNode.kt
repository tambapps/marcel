package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

abstract class AbstractStatementCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), StatementCstNode {

  constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

}