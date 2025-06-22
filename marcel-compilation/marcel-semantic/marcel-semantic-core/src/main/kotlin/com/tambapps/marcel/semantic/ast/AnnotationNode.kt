package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation
import com.tambapps.marcel.semantic.symbol.type.JavaAnnotationType

class AnnotationNode constructor(
  override val type: JavaAnnotationType,
  val attributes: List<JavaAnnotation.Attribute>,
  override val tokenStart: LexToken,
  override val tokenEnd: LexToken,
  val identifierToken: LexToken? = null
) : JavaAnnotation, AstNode {

  override fun getAttribute(name: String): JavaAnnotation.Attribute? {
    type.attributes.find { it.name == name }
    return attributes.find { it.name == name }
    // find on default values
      ?: type.attributes.find { it.name == name && it.defaultValue != null }
        ?.let { JavaAnnotation.Attribute(it.name, it.type, it.defaultValue!!) }
  }
}