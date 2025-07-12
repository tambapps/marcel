package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

sealed class AbstractMethodCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken,
                                   val accessNode: AccessCstNode,
  val identifierToken: LexToken? = null) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

  var isVarArgs = false
  // not supporting declaring abstract methods for now
  val isAbstract: Boolean get() = false
  val parameters = mutableListOf<MethodParameterCstNode>()
  val annotations = mutableListOf<AnnotationCstNode>()
  val statements = mutableListOf<StatementCstNode>()

}