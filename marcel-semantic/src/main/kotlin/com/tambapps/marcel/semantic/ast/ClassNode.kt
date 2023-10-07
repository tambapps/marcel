package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

class ClassNode(
  override val type: JavaType,
  val visibility: Visibility,
  tokenStart: LexToken, tokenEnd: LexToken
) : AbstractAst2Node(tokenStart, tokenEnd), JavaTyped {

  val imports = mutableListOf<ImportNode>()
  val methods = mutableListOf<MethodNode>()
  val annotations = mutableListOf<AnnotationNode>()

  fun addMethod(method: MethodNode) {
    // TODO check for methods with same signature and throw exception if already has one
    methods.add(method)
  }
}