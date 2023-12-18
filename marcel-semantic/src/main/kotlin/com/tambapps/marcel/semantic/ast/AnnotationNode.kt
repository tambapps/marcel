package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.type.JavaAnnotation
import com.tambapps.marcel.semantic.type.JavaAnnotationType

class AnnotationNode constructor(
  val annotationType: JavaAnnotationType,
  val attributes: List<JavaAnnotation.Attribute>,
  override val tokenStart: LexToken, override val tokenEnd: LexToken) : Ast2Node {
}