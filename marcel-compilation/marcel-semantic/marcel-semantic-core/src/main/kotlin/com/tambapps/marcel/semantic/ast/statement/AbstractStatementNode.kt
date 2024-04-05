package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AbstractAstNode

abstract class AbstractStatementNode(tokenStart: LexToken, tokenEnd: LexToken) : AbstractAstNode(tokenStart, tokenEnd),
  StatementNode {

  constructor(token: LexToken) : this(token, token)
  constructor(node: CstNode) : this(node.tokenStart, node.tokenEnd)
}