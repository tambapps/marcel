package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

class ScriptCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  className: String
) : ClassCstNode(tokenStart, tokenEnd, className) {

  val runMethodStatements: MutableList<StatementCstNode> = mutableListOf()

  val isNotEmpty get() = runMethodStatements.isNotEmpty()
      || methods.isNotEmpty()
      || fields.isNotEmpty()
      || constructors.isNotEmpty()

}