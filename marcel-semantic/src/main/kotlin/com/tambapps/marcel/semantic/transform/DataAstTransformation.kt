package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode

class DataAstTransformation:
  CompositeAstTransformation(listOf(
    StringifyAstTransformation(),
    EqualsAndHashcodeAstTransformation()
  )) {
  override fun shouldApply(transformation: AstTransformation, annotation: AnnotationNode) = when (transformation) {
    is StringifyAstTransformation -> annotation.getAttribute("stringify")?.value != false
    else -> true
  }
}