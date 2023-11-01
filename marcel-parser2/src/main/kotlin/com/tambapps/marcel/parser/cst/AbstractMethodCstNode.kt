package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

sealed class AbstractMethodCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken,
                                   val accessNode: CstAccessNode) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

  val parameters = mutableListOf<MethodParameterCstNode>()
  val annotations = mutableListOf<AnnotationCstNode>()
  val statements = mutableListOf<StatementCstNode>()
}