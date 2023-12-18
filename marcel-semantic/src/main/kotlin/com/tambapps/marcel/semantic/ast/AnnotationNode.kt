package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaAnnotation
import com.tambapps.marcel.semantic.type.JavaType

class AnnotationNode constructor(
  val annotationType: JavaAnnotation,
  val attributeNodes: List<AttributeNode>,
  override val tokenStart: LexToken, override val tokenEnd: LexToken) : Ast2Node {
    data class AttributeNode(val name: String, val type: JavaType, val value: Any)
}