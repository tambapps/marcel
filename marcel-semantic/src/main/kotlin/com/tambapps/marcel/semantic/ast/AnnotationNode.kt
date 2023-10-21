package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.LoadedJavaAnnotation

class AnnotationNode constructor(
  type: JavaType,
                                            // JavaConstantExpression
  val attributeNodes: List<AttributeNode>,
  override val tokenStart: LexToken, override val tokenEnd: LexToken) : Ast2Node, LoadedJavaAnnotation(type) {
    data class AttributeNode(val name: String, val type: JavaType, val value: Any)
}