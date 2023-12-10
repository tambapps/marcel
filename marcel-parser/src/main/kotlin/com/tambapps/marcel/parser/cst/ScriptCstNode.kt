package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.statement.StatementNode

class ScriptCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  className: String
) : ClassCstNode(tokenStart, tokenEnd,
  AccessNode(null, tokenStart, tokenEnd, false, false, false, TokenType.VISIBILITY_PUBLIC, false),
  className, null, emptyList(), null
) {

  val runMethodStatements: MutableList<StatementNode> = mutableListOf()

  val isNotEmpty get() = runMethodStatements.isNotEmpty()
      || methods.isNotEmpty()
      || fields.isNotEmpty()
      || constructors.isNotEmpty()

}