package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.statement.StatementCstNode

// only handle scripts for now
class SourceFileCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val fileName: String, // without extension
  ):
    AbstractCstNode(null, tokenStart, tokenEnd) {
    val statements: MutableList<StatementCstNode> = mutableListOf()
  val methods: MutableList<MethodCstNode> = mutableListOf()
  val fields: MutableList<FieldCstNode> = mutableListOf()
  val constructors: MutableList<ConstructorCstNode> = mutableListOf()
}