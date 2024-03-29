package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

class ScriptCstNode(
  parentSourceFileNode: SourceFileCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  className: String
) : ClassCstNode(parentSourceFileNode, tokenStart, tokenEnd,
  AccessCstNode(parentSourceFileNode, tokenStart, tokenEnd, false, false, false, TokenType.VISIBILITY_PUBLIC, false),
  className, null, emptyList(), null
) {

  val runMethodStatements: MutableList<StatementCstNode> = mutableListOf()

  val isNotEmpty get() = runMethodStatements.isNotEmpty()
      || methods.isNotEmpty()
      || fields.isNotEmpty()
      || constructors.isNotEmpty()

}