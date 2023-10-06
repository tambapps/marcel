package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaType

class ClassNode(val type: JavaType, tokenStart: LexToken, tokenEnd: LexToken) : AbstractAst2Node(tokenStart, tokenEnd) {

  val methods = mutableListOf<MethodNode>()

  fun addMethod(method: MethodNode) {
    // TODO check for methods with same signature and throw exception if already has one
    methods.add(method)
  }
}