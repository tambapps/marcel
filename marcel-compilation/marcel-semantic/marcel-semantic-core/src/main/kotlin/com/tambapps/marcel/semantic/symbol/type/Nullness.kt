package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import org.jspecify.annotations.NonNull
import org.jspecify.annotations.Nullable


enum class Nullness {
  /**
   * The value can be null
   */
  NULLABLE,
  /**
   * The value can't be null
   */
  NOT_NULL,
  /**
   * The value may or may not be null
   */
  UNKNOWN;

  companion object {
    fun nullableAnnotation(node: AstNode) = nullableAnnotation(node.tokenStart, node.tokenEnd)
    fun nullableAnnotation(tokenStart: LexToken, tokenEnd: LexToken) = annotation(Nullable::class.javaAnnotationType, tokenStart, tokenEnd)

    fun nonNullAnnotation(node: AstNode) = nonNullAnnotation(node.tokenStart, node.tokenEnd)
    fun nonNullAnnotation(tokenStart: LexToken, tokenEnd: LexToken) = annotation(NonNull::class.javaAnnotationType, tokenStart, tokenEnd)

    private fun annotation(annotationType: JavaAnnotationType, tokenStart: LexToken, tokenEnd: LexToken) = AnnotationNode(
      annotationType,
      emptyList(),
      tokenStart, tokenEnd
    )
  }
}