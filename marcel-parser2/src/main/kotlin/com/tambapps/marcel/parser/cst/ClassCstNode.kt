package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken

open class ClassCstNode(
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val className: String,
) : AbstractCstNode(null, tokenStart, tokenEnd) {

  val methods: MutableList<MethodCstNode> = mutableListOf()
  val fields: MutableList<FieldCstNode> = mutableListOf()
  val constructors: MutableList<ConstructorCstNode> = mutableListOf()
  val innerClasses: MutableList<ClassCstNode> = mutableListOf()

}