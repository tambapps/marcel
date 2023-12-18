package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaAnnotationType
import com.tambapps.marcel.semantic.type.JavaType

class AnnotationNode constructor(
  val annotationType: JavaAnnotationType,
  val attributeNodes: List<AttributeNode>,
  override val tokenStart: LexToken, override val tokenEnd: LexToken) : Ast2Node {
    data class AttributeNode(val name: String, val type: JavaType, val value: Any)
}