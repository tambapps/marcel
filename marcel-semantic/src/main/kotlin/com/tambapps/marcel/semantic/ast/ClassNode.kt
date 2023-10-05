package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken

class ClassNode(val className: String, tokenStart: LexToken, tokenEnd: LexToken) : AbstractAst2Node(tokenStart, tokenEnd) {

  val methods = mutableListOf<MethodeNode>()

  fun addMethod(method: MethodeNode) {
    // TODO check for methods with same signature and throw exception if already has one
    methods.add(method)
  }
}