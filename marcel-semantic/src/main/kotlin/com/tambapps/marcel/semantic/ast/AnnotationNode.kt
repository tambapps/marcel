package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaAnnotation
import com.tambapps.marcel.semantic.type.JavaAnnotationType

class AnnotationNode constructor(
  override val type: JavaAnnotationType,
  val attributes: List<JavaAnnotation.Attribute>,
  override val tokenStart: LexToken, override val tokenEnd: LexToken
) : JavaAnnotation, AstNode {

  override fun getAttribute(name: String): JavaAnnotation.Attribute? {
    return attributes.find { it.name == name }
  }
}